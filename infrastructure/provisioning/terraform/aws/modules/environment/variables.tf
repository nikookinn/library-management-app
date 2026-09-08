variable "environment" {
  description = "Environment name (dev, staging, or production)."
  type        = string

  validation {
    condition     = contains(["dev", "staging", "production"], var.environment)
    error_message = "environment must be one of: dev, staging, production."
  }
}

variable "project_name" {
  description = "Short name used as a prefix/tag for every resource this module creates."
  type        = string
}

variable "vpc_id" {
  description = "ID of the shared VPC (from the global layer), for environment-scoped resources that need it."
  type        = string
}

variable "tags" {
  description = "Extra tags merged into every resource this module creates."
  type        = map(string)
  default     = {}
}
