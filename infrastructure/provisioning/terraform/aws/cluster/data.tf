# Reads the global layer's outputs (VPC and subnet IDs) from its state file. The cluster layer
# depends on global, never the other way around.
data "terraform_remote_state" "global" {
  backend = "s3"

  config = {
    bucket = var.terraform_state_bucket
    key    = "global/terraform.tfstate"
    region = var.aws_region
  }
}
