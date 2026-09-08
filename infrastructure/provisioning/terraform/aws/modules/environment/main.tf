# This module exists so dev/staging/production don't duplicate resource blocks once they need
# environment-specific AWS resources (like External Secrets IAM policies, backup buckets, or DNS
# records). We don't need any of that yet, so for now this module only sets the standard tags
# every future resource here will use. Add new resources here, not by copying blocks into each
# environments/<env>/main.tf.

locals {
  common_tags = merge(
    {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "terraform"
      Layer       = "environment"
    },
    var.tags,
  )
}
