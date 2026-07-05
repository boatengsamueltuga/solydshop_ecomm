#!/usr/bin/env bash
set -euo pipefail

# One-time setup for a fresh Oracle Cloud Ubuntu VM. Run as: bash vm-bootstrap.sh

sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg

sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

sudo usermod -aG docker "$USER"

# Oracle's default Ubuntu image blocks 80/443 at the iptables level in
# addition to the Cloud console's security list - both must be opened.
sudo iptables -I INPUT -p tcp --dport 80 -j ACCEPT
sudo iptables -I INPUT -p tcp --dport 443 -j ACCEPT
sudo netfilter-persistent save 2>/dev/null || true

mkdir -p ~/solydshop

echo "Docker installed. Log out and back in for group membership to take effect, then copy docker-compose.yml, Caddyfile, and .env into ~/solydshop/"
