# Partial backend config on purpose: the real bucket, key, and region live in backend.hcl, loaded with
# `terraform init -backend-config=backend.hcl`. This way the file itself never changes per environment
# or account. Note: this layer creates its own state bucket, so the very first apply has to run with
# local state first, then move to S3 once the bucket exists.
terraform {
  backend "s3" {}
}
