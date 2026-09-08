variable "aws_region" {
  description = "AWS region. Must match the region the global layer's VPC was created in."
  type        = string
  default     = "eu-central-1"
}

variable "project_name" {
  description = "Short name used as a prefix/tag for every resource this layer creates."
  type        = string
  default     = "library-management-app"
}

variable "terraform_state_bucket" {
  description = "Name of the shared Terraform state bucket, used to read the global layer's outputs."
  type        = string
  default     = "library-management-app-tfstate"
}
