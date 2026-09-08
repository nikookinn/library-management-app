variable "aws_region" {
  description = "AWS region for all global resources (VPC, state bucket, IAM)."
  type        = string
  default     = "eu-central-1"
}

variable "project_name" {
  description = "Short name used as a prefix/tag for every resource this layer creates."
  type        = string
  default     = "library-management-app"
}

variable "vpc_cidr" {
  description = "CIDR block for the shared VPC that the kubeadm cluster and its environments run in."
  type        = string
  default     = "10.20.0.0/16"
}

variable "availability_zones" {
  description = "Availability zones to spread the public and private subnets across."
  type        = list(string)
  default     = ["eu-central-1a", "eu-central-1b"]
}

variable "single_nat_instance" {
  description = "If true (default), use one shared NAT instance for all private subnets (cheaper, but a single point of failure). If false, use one NAT instance per availability zone (more reliable, roughly doubles the NAT cost)."
  type        = bool
  default     = true
}

variable "nat_instance_type" {
  description = "EC2 instance type for the NAT instance(s). Defaults to `t4g.micro`, which is free under the AWS Free Tier (750 hours/month, shared with other t2.micro/t3.micro/t4g.micro usage, for a new account's first 12 months) and is enough for our current traffic."
  type        = string
  default     = "t4g.micro"
}

variable "github_organization" {
  description = "GitHub organization/user that owns this repository, used to scope the OIDC trust policy."
  type        = string
  default     = "nikookinn"
}

variable "github_repository" {
  description = "GitHub repository name (without the organization), used to scope the OIDC trust policy."
  type        = string
  default     = "library-management-app"
}

variable "github_actions_trusted_branches" {
  description = "Branches allowed to assume the github-actions-terraform IAM role via OIDC."
  type        = list(string)
  default     = ["main", "dev", "staging", "production"]
}
