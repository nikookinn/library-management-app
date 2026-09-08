# Calls the shared environment module so dev/staging/production don't repeat the same resource
# blocks. We don't need any environment-specific AWS resource yet, so this module currently just
# adds tags.
module "environment" {
  source = "../../modules/environment"

  environment  = "staging"
  project_name = var.project_name
  vpc_id       = data.terraform_remote_state.global.outputs.vpc_id
}
