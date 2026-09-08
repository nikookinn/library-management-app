# Values for `terraform init -backend-config=backend.hcl` in this layer.
# The bucket name must be globally unique across all of AWS, so it is not a Terraform variable (backend
# configuration cannot reference resources, variables, or data sources). Change it before the first apply if
# it collides with a bucket someone else already owns.
bucket       = "library-management-app-tfstate"
key          = "global/terraform.tfstate"
region       = "eu-central-1"
encrypt      = true
use_lockfile = true
