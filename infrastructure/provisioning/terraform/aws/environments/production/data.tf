# Reads the global layer's outputs. This environment only depends on global, never on cluster
# or on another environment's state.
data "terraform_remote_state" "global" {
  backend = "s3"

  config = {
    bucket = var.terraform_state_bucket
    key    = "global/terraform.tfstate"
    region = var.aws_region
  }
}
