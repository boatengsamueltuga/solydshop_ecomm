#!/usr/bin/env bash
set -euo pipefail

# One-time setup for a fresh DigitalOcean Ubuntu droplet. Run as: bash vm-bootstrap.sh

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

# DigitalOcean droplets don't block 80/443 at the iptables level the way
# Oracle's default image did - just make sure inbound 22/80/443 are
# allowed in a DigitalOcean Cloud Firewall attached to the droplet (or in
# ufw, if you're using it instead).

mkdir -p ~/solydshop

echo "Docker installed. Log out and back in for group membership to take effect, then copy docker-compose.yml, Caddyfile, and .env into ~/solydshop/"
