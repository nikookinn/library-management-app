output "cluster_node_security_group_id" {
  description = "Security group future kubeadm node EC2 instances will use."
  value       = aws_security_group.cluster_nodes.id
}

output "cluster_ingress_security_group_id" {
  description = "Boundary security group the future load balancer/Gateway API implementation fronting the cluster will use."
  value       = aws_security_group.cluster_ingress.id
}

output "cluster_node_instance_profile_name" {
  description = "IAM instance profile (SSM-enabled) future kubeadm node EC2 instances will use."
  value       = aws_iam_instance_profile.cluster_node.name
}

output "private_subnet_ids" {
  description = "Private subnet IDs future kubeadm node EC2 instances (control plane and autoscaled workers) will be placed in."
  value       = local.private_subnet_ids
}
