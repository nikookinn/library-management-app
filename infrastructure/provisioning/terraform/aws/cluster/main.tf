# This only sets up the shared resources for the kubeadm cluster. The actual EC2 instances and
# the kubeadm setup itself come in a later phase, using Ansible/Packer on top of this.

locals {
  vpc_id             = data.terraform_remote_state.global.outputs.vpc_id
  public_subnet_ids  = data.terraform_remote_state.global.outputs.public_subnet_ids
  private_subnet_ids = data.terraform_remote_state.global.outputs.private_subnet_ids
}

# Security group for whatever sits in front of the cluster later (the load balancer for our
# Gateway API setup). This is the only group ever allowed to be open to the internet on
# 80/443; nodes never get a public IP or a rule open to 0.0.0.0/0 directly.
resource "aws_security_group" "cluster_ingress" {
  name        = "${var.project_name}-cluster-ingress"
  description = "Boundary for the future load balancer/Gateway API implementation fronting the kubeadm cluster"
  vpc_id      = local.vpc_id

  ingress {
    description = "HTTP from the internet"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTPS from the internet"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    description = "All outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-cluster-ingress"
  }
}

# Security group for the future kubeadm nodes. No SSH rule: node access goes through AWS
# Systems Manager Session Manager instead. Nodes live in private subnets (no public IP) and can
# only be reached from inside this group or from the ingress group above, never from the
# internet directly.
resource "aws_security_group" "cluster_nodes" {
  name        = "${var.project_name}-cluster-nodes"
  description = "Shared security group for the kubeadm cluster's nodes"
  vpc_id      = local.vpc_id

  # Node-to-node traffic (kubelet, etcd, pod network) stays inside the security group itself.
  ingress {
    description = "All traffic between cluster nodes"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    self        = true
  }

  # Traffic from the future load balancer to the ingress controller running on the nodes,
  # either via hostNetwork (80/443) or a NodePort service (30000-32767 range).
  ingress {
    description     = "HTTP from the cluster ingress boundary"
    from_port       = 80
    to_port         = 80
    protocol        = "tcp"
    security_groups = [aws_security_group.cluster_ingress.id]
  }

  ingress {
    description     = "HTTPS from the cluster ingress boundary"
    from_port       = 443
    to_port         = 443
    protocol        = "tcp"
    security_groups = [aws_security_group.cluster_ingress.id]
  }

  ingress {
    description     = "NodePort range from the cluster ingress boundary"
    from_port       = 30000
    to_port         = 32767
    protocol        = "tcp"
    security_groups = [aws_security_group.cluster_ingress.id]
  }

  egress {
    description = "All outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-cluster-nodes"
  }
}

# IAM role and instance profile for future kubeadm node instances, giving SSM Session Manager
# access instead of an SSH key pair. Only EC2 can assume this role - no human and no other AWS service can,
# and no human is ever given this role directly (see the platform_operators group in the global layer for
# how a person gets SSM access instead).
data "aws_iam_policy_document" "cluster_node_assume_role" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "cluster_node" {
  name               = "${var.project_name}-cluster-node"
  assume_role_policy = data.aws_iam_policy_document.cluster_node_assume_role.json
  description        = "Assumed by future kubeadm node EC2 instances. SSM-only administrative access, no SSH."
}

# Today this role only needs SSM (so a human can reach the node through Session Manager if needed, and so
# the node can report its status back to AWS). When a real node responsibility shows up later - for example
# reading a secret from Secrets Manager, or an Auto Scaling Group needing to describe/terminate itself for
# lifecycle hooks - attach a new, narrowly-scoped policy to this same role rather than widening this one.
# Do not attach anything broader (e.g. a managed *FullAccess policy) than the specific actions that new
# feature needs.
resource "aws_iam_role_policy_attachment" "cluster_node_ssm" {
  role       = aws_iam_role.cluster_node.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_instance_profile" "cluster_node" {
  name = "${var.project_name}-cluster-node"
  role = aws_iam_role.cluster_node.name
}
