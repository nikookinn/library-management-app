#!/bin/bash
# Turns this instance into a NAT for the private subnets. Runs once on first boot via
# cloud-init user data.
set -euxo pipefail

# Allow this instance to forward traffic that is not addressed to itself (disabled by default on every EC2
# instance). Persisted so it survives a reboot, not just the current boot.
echo "net.ipv4.ip_forward = 1" >/etc/sysctl.d/99-nat-instance.conf
sysctl --system

# Masquerade (SNAT) outbound traffic from the private subnets behind this instance's primary network
# interface, so responses come back to this instance rather than needing a route back to the private CIDR.
PRIMARY_IFACE="$(ip -o -4 route show to default | awk '{print $5}' | head -n1)"
iptables -t nat -A POSTROUTING -o "${PRIMARY_IFACE}" -j MASQUERADE
iptables -P FORWARD ACCEPT

# Persist the iptables rules and restore them on every boot (the rule above only affects the running kernel
# state, and does not survive a reboot on its own).
mkdir -p /etc/iptables
iptables-save >/etc/iptables/rules.v4

cat >/etc/systemd/system/nat-instance-iptables-restore.service <<'UNIT'
[Unit]
Description=Restore NAT instance iptables rules
After=network-online.target
Wants=network-online.target

[Service]
Type=oneshot
ExecStart=/sbin/iptables-restore /etc/iptables/rules.v4
RemainAfterExit=yes

[Install]
WantedBy=multi-user.target
UNIT

systemctl daemon-reload
systemctl enable --now nat-instance-iptables-restore.service
