data "aws_caller_identity" "current" {}

# --- Remote state storage -----------------------------------------------------------------
# This bucket stores the Terraform state for every layer. Versioning is on, so we can recover
# from a bad apply. Encrypted at rest, and never publicly reachable.

resource "aws_s3_bucket" "terraform_state" {
  bucket = "${var.project_name}-tfstate"

  # Never destroy the state bucket via `terraform destroy` in this layer by accident.
  lifecycle {
    prevent_destroy = true
  }
}

resource "aws_s3_bucket_versioning" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id

  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
    bucket_key_enabled = true
  }
}

resource "aws_s3_bucket_public_access_block" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_policy" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id
  policy = data.aws_iam_policy_document.terraform_state_tls_only.json
}

data "aws_iam_policy_document" "terraform_state_tls_only" {
  statement {
    sid    = "DenyInsecureTransport"
    effect = "Deny"

    principals {
      type        = "*"
      identifiers = ["*"]
    }

    actions = ["s3:*"]

    resources = [
      aws_s3_bucket.terraform_state.arn,
      "${aws_s3_bucket.terraform_state.arn}/*",
    ]

    condition {
      test     = "Bool"
      variable = "aws:SecureTransport"
      values   = ["false"]
    }
  }
}

# --- Shared networking ---------------------------------------------------------------------
# VPC, public and private subnets, NAT egress, and route tables. The NAT instance(s) below are
# the only EC2 instances created here, and they only give the private subnets internet access.
# No Kubernetes node or workload is created yet.
#
# Public subnets are only for things that must be reachable from the internet: the NAT
# instance(s) now, and later the load balancer in front of the cluster. Kubernetes nodes always
# stay in the private subnets defined below.

resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name = "${var.project_name}-vpc"
  }
}

resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name = "${var.project_name}-igw"
  }
}

resource "aws_subnet" "public" {
  for_each = { for idx, az in var.availability_zones : az => idx }

  vpc_id                  = aws_vpc.main.id
  availability_zone       = each.key
  cidr_block              = cidrsubnet(var.vpc_cidr, 8, each.value)
  map_public_ip_on_launch = true

  tags = {
    Name = "${var.project_name}-public-${each.key}"
    Tier = "public"
  }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = {
    Name = "${var.project_name}-public-rt"
  }
}

resource "aws_route_table_association" "public" {
  for_each = aws_subnet.public

  subnet_id      = each.value.id
  route_table_id = aws_route_table.public.id
}

# --- Private subnets (Kubernetes nodes) -----------------------------------------------------
# We add +100 to the subnet index so private CIDRs never overlap with the public ones above,
# while still using the same /16 and the same cidrsubnet() pattern.

resource "aws_subnet" "private" {
  for_each = { for idx, az in var.availability_zones : az => idx }

  vpc_id                  = aws_vpc.main.id
  availability_zone       = each.key
  cidr_block              = cidrsubnet(var.vpc_cidr, 8, each.value + 100)
  map_public_ip_on_launch = false

  tags = {
    Name = "${var.project_name}-private-${each.key}"
    Tier = "private"
  }
}

# --- NAT egress for private subnets -----------------------------------------------------------
# Nodes in the private subnets need outbound internet access (OS updates, pulling container
# images, the SSM agent) but must never be reached directly from the internet. We use a small,
# self-managed NAT instance (a plain EC2 instance forwarding traffic with iptables) instead of a
# managed NAT Gateway, because it is much cheaper for our current traffic.
# `single_nat_instance = true` (default) creates one NAT instance in the first availability
# zone, shared by every private route table. Set it to `false` to get one NAT instance per
# zone instead, which removes this single point of failure but roughly doubles the cost. Unlike
# a NAT Gateway, a failed NAT instance is not replaced automatically.

locals {
  nat_instance_azs = var.single_nat_instance ? [var.availability_zones[0]] : var.availability_zones
}

data "aws_ami" "nat_instance" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-kernel-*-arm64"]
  }

  filter {
    name   = "architecture"
    values = ["arm64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# Kept separate from the cluster_nodes security group in cluster/main.tf: this is not a
# Kubernetes node, and it only needs traffic coming from inside the VPC, never from the internet.
resource "aws_security_group" "nat_instance" {
  name        = "${var.project_name}-nat-instance"
  description = "Allows private subnet egress traffic to be forwarded/masqueraded by the NAT instance(s)"
  vpc_id      = aws_vpc.main.id

  ingress {
    description = "All traffic from within the VPC (private subnets routing their egress through this instance)"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = [var.vpc_cidr]
  }

  egress {
    description = "All outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-nat-instance"
  }
}

data "aws_iam_policy_document" "nat_instance_assume_role" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "nat_instance" {
  name               = "${var.project_name}-nat-instance"
  assume_role_policy = data.aws_iam_policy_document.nat_instance_assume_role.json
  description        = "Assumed by the NAT instance(s) EC2 profile. SSM-only administrative access, no SSH."
}

# Same access model as the future kubeadm nodes: SSM Session Manager only, no SSH key pair, no
# inbound rule needed for it.
resource "aws_iam_role_policy_attachment" "nat_instance_ssm" {
  role       = aws_iam_role.nat_instance.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_instance_profile" "nat_instance" {
  name = "${var.project_name}-nat-instance"
  role = aws_iam_role.nat_instance.name
}

resource "aws_instance" "nat" {
  for_each = toset(local.nat_instance_azs)

  ami                    = data.aws_ami.nat_instance.id
  instance_type          = var.nat_instance_type
  subnet_id              = aws_subnet.public[each.key].id
  vpc_security_group_ids = [aws_security_group.nat_instance.id]
  iam_instance_profile   = aws_iam_instance_profile.nat_instance.name

  # Required for a NAT instance: by default EC2 drops any packet not addressed to/from the instance itself,
  # which would silently break forwarding for every private-subnet node behind it.
  source_dest_check = false

  user_data                   = file("${path.module}/nat-instance-user-data.sh")
  user_data_replace_on_change = true

  tags = {
    Name = "${var.project_name}-nat-${each.key}"
  }
}

# A fixed public IP for the NAT instance(s). If the instance is ever replaced (for example by
# user_data_replace_on_change), it keeps the same IP, so we don't need to update anything else.
resource "aws_eip" "nat" {
  for_each = toset(local.nat_instance_azs)

  domain   = "vpc"
  instance = aws_instance.nat[each.key].id

  tags = {
    Name = "${var.project_name}-nat-eip-${each.key}"
  }

  depends_on = [aws_internet_gateway.main]
}

resource "aws_route_table" "private" {
  for_each = { for idx, az in var.availability_zones : az => idx }

  vpc_id = aws_vpc.main.id

  route {
    cidr_block           = "0.0.0.0/0"
    network_interface_id = var.single_nat_instance ? aws_instance.nat[local.nat_instance_azs[0]].primary_network_interface_id : aws_instance.nat[each.key].primary_network_interface_id
  }

  tags = {
    Name = "${var.project_name}-private-rt-${each.key}"
  }
}

resource "aws_route_table_association" "private" {
  for_each = aws_subnet.private

  subnet_id      = each.value.id
  route_table_id = aws_route_table.private[each.key].id
}

# --- Human operator access (SSM Session Manager) ----------------------------------------------
# A human never gets an EC2 instance role (those are only assumed by EC2 itself) and never gets SSH access
# (no key pair is ever created in this project). Instead, a human is added to this IAM group, which grants
# just enough permission to open an AWS Systems Manager Session Manager shell into an instance that belongs
# to this project. Adding a real person to this group is done outside Terraform (by whoever administers IAM
# users in the account), since this project does not manage human user accounts.
data "aws_iam_policy_document" "ssm_operator_session_access" {
  # Only allowed against instances tagged with this project's name (every instance gets that tag from
  # `default_tags` in provider.tf), and only using the standard interactive-shell session document.
  statement {
    sid     = "StartSessionOnProjectInstances"
    effect  = "Allow"
    actions = ["ssm:StartSession"]
    resources = [
      "arn:aws:ec2:${var.aws_region}:${data.aws_caller_identity.current.account_id}:instance/*",
      "arn:aws:ssm:${var.aws_region}::document/SSM-SessionManagerRunShell",
    ]

    condition {
      test     = "StringEquals"
      variable = "ssm:resourceTag/Project"
      values   = [var.project_name]
    }
  }

  # A session's resource ID always starts with the caller's own IAM user name, so this scopes
  # terminate/resume to sessions the same person started - never someone else's session.
  statement {
    sid       = "ManageOwnSessionsOnly"
    effect    = "Allow"
    actions   = ["ssm:TerminateSession", "ssm:ResumeSession"]
    resources = ["arn:aws:ssm:*:*:session/$${aws:username}-*"]
  }

  # Read-only discovery calls. These do not support resource-level restriction, so the resource has to stay
  # "*" - but the actions themselves are limited to listing/describing, no ability to change anything.
  statement {
    sid    = "ListSessionsAndInstances"
    effect = "Allow"
    actions = [
      "ssm:DescribeSessions",
      "ssm:GetConnectionStatus",
      "ssm:DescribeInstanceInformation",
      "ec2:DescribeInstances",
      "ec2:DescribeInstanceStatus",
    ]
    resources = ["*"]
  }
}

resource "aws_iam_policy" "ssm_operator_session_access" {
  name        = "${var.project_name}-ssm-operator-session-access"
  description = "Lets a human operator open an SSM Session Manager shell into this project's EC2 instances. No SSH."
  policy      = data.aws_iam_policy_document.ssm_operator_session_access.json
}

resource "aws_iam_group" "platform_operators" {
  name = "${var.project_name}-platform-operators"
}

resource "aws_iam_group_policy_attachment" "platform_operators_ssm" {
  group      = aws_iam_group.platform_operators.name
  policy_arn = aws_iam_policy.ssm_operator_session_access.arn
}

# --- GitHub Actions authentication (OIDC, no static AWS credentials) -----------------------

resource "aws_iam_openid_connect_provider" "github_actions" {
  url            = "https://token.actions.githubusercontent.com"
  client_id_list = ["sts.amazonaws.com"]
  # GitHub's root CA thumbprint. AWS checks the TLS chain itself; this list just has to be non-empty.
  thumbprint_list = ["6938fd4d98bab03faadb97b34396831e3780aea1"]
}

data "aws_iam_policy_document" "github_actions_trust" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type        = "Federated"
      identifiers = [aws_iam_openid_connect_provider.github_actions.arn]
    }

    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:aud"
      values   = ["sts.amazonaws.com"]
    }

    condition {
      test     = "StringLike"
      variable = "token.actions.githubusercontent.com:sub"
      values = [
        for branch in var.github_actions_trusted_branches :
        "repo:${var.github_organization}/${var.github_repository}:ref:refs/heads/${branch}"
      ]
    }
  }
}

resource "aws_iam_role" "github_actions_terraform" {
  name               = "github-actions-terraform"
  assume_role_policy = data.aws_iam_policy_document.github_actions_trust.json
  description        = "Assumed by GitHub Actions via OIDC to run Terraform. No static AWS credentials are used."
}

# Least-privilege action lists instead of "ec2:*"/"iam:*". Most of these EC2 actions don't support
# resource-level ARNs at all, so the resource is still "*" for that statement, but the action list itself
# is scoped to only what the resources in this repo's Terraform actually create today (VPC, subnets, route
# tables, security groups, EC2 instances, Elastic IPs). Extend this list only when a new resource block is
# added, not ahead of time.
data "aws_iam_policy_document" "github_actions_terraform_permissions" {
  statement {
    sid    = "ManageNetworking"
    effect = "Allow"
    actions = [
      "ec2:DescribeVpcs",
      "ec2:CreateVpc",
      "ec2:DeleteVpc",
      "ec2:ModifyVpcAttribute",
      "ec2:DescribeVpcAttribute",
      "ec2:DescribeInternetGateways",
      "ec2:CreateInternetGateway",
      "ec2:DeleteInternetGateway",
      "ec2:AttachInternetGateway",
      "ec2:DetachInternetGateway",
      "ec2:DescribeSubnets",
      "ec2:CreateSubnet",
      "ec2:DeleteSubnet",
      "ec2:ModifySubnetAttribute",
      "ec2:DescribeRouteTables",
      "ec2:CreateRouteTable",
      "ec2:DeleteRouteTable",
      "ec2:CreateRoute",
      "ec2:DeleteRoute",
      "ec2:ReplaceRoute",
      "ec2:AssociateRouteTable",
      "ec2:DisassociateRouteTable",
      "ec2:ReplaceRouteTableAssociation",
      "ec2:DescribeSecurityGroups",
      "ec2:DescribeSecurityGroupRules",
      "ec2:CreateSecurityGroup",
      "ec2:DeleteSecurityGroup",
      "ec2:AuthorizeSecurityGroupIngress",
      "ec2:RevokeSecurityGroupIngress",
      "ec2:AuthorizeSecurityGroupEgress",
      "ec2:RevokeSecurityGroupEgress",
      "ec2:UpdateSecurityGroupRuleDescriptionsIngress",
      "ec2:UpdateSecurityGroupRuleDescriptionsEgress",
      "ec2:DescribeAvailabilityZones",
      "ec2:DescribeAccountAttributes",
      "ec2:DescribeNetworkInterfaces",
      "ec2:DescribeNetworkInterfaceAttribute",
      "ec2:ModifyNetworkInterfaceAttribute",
      "ec2:DescribeTags",
      "ec2:CreateTags",
      "ec2:DeleteTags",
    ]
    resources = ["*"]
  }

  statement {
    sid    = "ManageComputeForNatAndFutureNodes"
    effect = "Allow"
    actions = [
      "ec2:DescribeImages",
      "ec2:DescribeInstances",
      "ec2:DescribeInstanceStatus",
      "ec2:DescribeInstanceTypes",
      "ec2:DescribeInstanceCreditSpecifications",
      "ec2:DescribeVolumes",
      "ec2:RunInstances",
      "ec2:TerminateInstances",
      "ec2:StopInstances",
      "ec2:StartInstances",
      "ec2:ModifyInstanceAttribute",
      "ec2:DescribeAddresses",
      "ec2:AllocateAddress",
      "ec2:ReleaseAddress",
      "ec2:AssociateAddress",
      "ec2:DisassociateAddress",
    ]
    resources = ["*"]
  }

  statement {
    sid    = "ManageInstanceIamRoles"
    effect = "Allow"
    actions = [
      "iam:CreateRole",
      "iam:DeleteRole",
      "iam:GetRole",
      "iam:TagRole",
      "iam:UntagRole",
      "iam:CreatePolicy",
      "iam:DeletePolicy",
      "iam:GetPolicy",
      "iam:GetPolicyVersion",
      "iam:ListPolicyVersions",
      "iam:CreatePolicyVersion",
      "iam:DeletePolicyVersion",
      "iam:AttachRolePolicy",
      "iam:DetachRolePolicy",
      "iam:PutRolePolicy",
      "iam:DeleteRolePolicy",
      "iam:GetRolePolicy",
      "iam:CreateInstanceProfile",
      "iam:DeleteInstanceProfile",
      "iam:GetInstanceProfile",
      "iam:AddRoleToInstanceProfile",
      "iam:RemoveRoleFromInstanceProfile",
      "iam:CreateOpenIDConnectProvider",
      "iam:GetOpenIDConnectProvider",
      "iam:UpdateOpenIDConnectProviderThumbprint",
      "iam:TagOpenIDConnectProvider",
      "iam:CreateGroup",
      "iam:DeleteGroup",
      "iam:GetGroup",
      "iam:AttachGroupPolicy",
      "iam:DetachGroupPolicy",
      "iam:ListAttachedGroupPolicies",
    ]
    resources = ["*"]
  }

  # iam:PassRole is how RunInstances attaches an instance profile's role to a new EC2 instance. It is kept in
  # its own statement, scoped to only the instance roles this project actually creates, so this identity can
  # never pass an unrelated, more privileged role to an instance (the classic PassRole privilege-escalation
  # path).
  statement {
    sid     = "PassOnlyThisProjectsInstanceRoles"
    effect  = "Allow"
    actions = ["iam:PassRole"]
    resources = [
      aws_iam_role.nat_instance.arn,
      "arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/${var.project_name}-cluster-node",
    ]
  }

  statement {
    sid    = "ManageTerraformStateBucket"
    effect = "Allow"
    actions = [
      "s3:GetObject",
      "s3:PutObject",
      "s3:DeleteObject",
      "s3:ListBucket",
      "s3:GetBucketVersioning",
      "s3:GetEncryptionConfiguration",
    ]
    resources = [
      aws_s3_bucket.terraform_state.arn,
      "${aws_s3_bucket.terraform_state.arn}/*",
    ]
  }
}

resource "aws_iam_role_policy" "github_actions_terraform" {
  name   = "terraform-provisioning"
  role   = aws_iam_role.github_actions_terraform.id
  policy = data.aws_iam_policy_document.github_actions_terraform_permissions.json
}
