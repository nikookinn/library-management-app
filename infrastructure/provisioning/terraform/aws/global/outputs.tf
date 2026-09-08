output "aws_account_id" {
  description = "AWS account ID this layer was applied against, for cross-checking backend.hcl files."
  value       = data.aws_caller_identity.current.account_id
}

output "terraform_state_bucket" {
  description = "Name of the S3 bucket every layer's backend.hcl points at."
  value       = aws_s3_bucket.terraform_state.bucket
}

output "vpc_id" {
  description = "ID of the shared VPC, consumed by the cluster and environment layers via terraform_remote_state."
  value       = aws_vpc.main.id
}

output "public_subnet_ids" {
  description = "IDs of the public subnets, consumed by the cluster layer. Reserved for internet-facing resources (the NAT instance(s), future load balancers) — Kubernetes nodes are never placed here."
  value       = [for subnet in aws_subnet.public : subnet.id]
}

output "private_subnet_ids" {
  description = "IDs of the private subnets, consumed by the cluster layer. Kubernetes nodes are placed here; egress-only internet access via the NAT instance(s) below."
  value       = [for subnet in aws_subnet.private : subnet.id]
}

output "nat_instance_ids" {
  description = "IDs of the NAT instance(s) providing outbound internet access to the private subnets."
  value       = [for instance in aws_instance.nat : instance.id]
}

output "nat_instance_public_ips" {
  description = "Elastic IP address(es) of the NAT instance(s)."
  value       = [for eip in aws_eip.nat : eip.public_ip]
}

output "github_actions_role_arn" {
  description = "ARN GitHub Actions assumes (via OIDC) to run Terraform. Used as the `role-to-assume` input of aws-actions/configure-aws-credentials."
  value       = aws_iam_role.github_actions_terraform.arn
}

output "platform_operators_group_name" {
  description = "IAM group name. Add a human IAM user to this group to let them open an SSM Session Manager shell into this project's EC2 instances. Group membership itself is managed outside Terraform."
  value       = aws_iam_group.platform_operators.name
}

output "ssm_operator_session_access_policy_arn" {
  description = "ARN of the managed policy attached to the platform_operators group. Useful if a human's SSM access needs to be granted directly on an existing IAM user instead of through the group."
  value       = aws_iam_policy.ssm_operator_session_access.arn
}
