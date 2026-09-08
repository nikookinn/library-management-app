# Partial backend config, same idea as global/backend.tf.
# The state bucket already exists by the time this layer is first applied, so no bootstrap step is needed.
terraform {
  backend "s3" {}
}
