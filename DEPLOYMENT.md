# Deployment Runbook

One-time setup to get SolydShop running in production for free. Code-level
changes referenced here are already in place (see
`docs/superpowers/specs/2026-07-04-free-deployment-design.md` in the
frontend repo for the full design).

## 1. Oracle Cloud VM

1. Sign up at https://www.oracle.com/cloud/free/ (Always Free tier).
2. Create a Compute instance: shape "Ampere A1 (VM.Standard.A1.Flex)",
   Ubuntu 24.04, at least 1 OCPU / 6GB RAM (well within the Always Free
   allowance of 4 OCPU / 24GB total).
3. Under Networking, reserve a **static** public IP for the instance
   (Oracle calls this a "Reserved Public IP") — a dynamic IP would break
   DNS/HTTPS on every reboot.
4. In the VM's attached Virtual Cloud Network → Security List, add ingress
   rules for TCP ports 22, 80, and 443 from source `0.0.0.0/0`.
5. Note the VM's public IP and download the SSH private key Oracle gives you.

## 2. DuckDNS

1. Sign up at https://www.duckdns.org (GitHub/Google login).
2. Create a subdomain (e.g. `solydshop.duckdns.org`) and point it at the
   VM's static public IP from step 1.

## 3. VM bootstrap

1. SSH into the VM: `ssh -i <key> ubuntu@<vm-ip>`
2. Copy `scripts/vm-bootstrap.sh` to the VM and run it: `bash vm-bootstrap.sh`
3. Log out and back in (for the `docker` group membership to apply).
4. Copy `docker-compose.yml`, `Caddyfile`, and `.env.example` into
   `~/solydshop/` on the VM (`scp` from your machine, or `git clone` the
   repo there and copy the files out).
5. On the VM, `cp .env.example .env` and fill in every real value —
   production Stripe/Cloudinary/mail credentials, a freshly generated
   `JWT_SECRET`, `DB_PASSWORD` (choose a new strong password, this is a
   fresh production database), `FRONTEND_URL`/`CORS_ALLOWED_ORIGINS` set to
   your Vercel URL (step 5 below), `COOKIE_SECURE=true`,
   `COOKIE_SAME_SITE=None`, and `DOMAIN` set to your DuckDNS hostname.
6. Start the stack: `cd ~/solydshop && docker compose up -d`
7. Verify: `curl https://<your-duckdns-domain>/actuator/health` from your
   own machine should return `{"status":"UP"}` over a valid HTTPS
   connection (Caddy auto-issues the certificate on first request — this
   may take a few seconds the first time).

## 4. GitHub Actions secrets (backend repo)

Add these under Settings → Secrets and variables → Actions:
- `VM_HOST` — the VM's public IP or DuckDNS hostname
- `VM_USER` — `ubuntu`
- `VM_SSH_KEY` — a **dedicated** deploy keypair's private key (don't reuse
  your personal key): generate with `ssh-keygen -t ed25519 -f deploy_key`,
  add `deploy_key.pub` to the VM's `~/.ssh/authorized_keys`, paste the
  contents of `deploy_key` (private half) as this secret.

By default, GHCR packages are private — after the first push from CI, go to
the package's settings on GitHub and make it public (simplest option,
since the image itself contains no secrets — those are injected via `.env`
at container runtime). Otherwise the VM's `docker compose pull` will fail
with an authentication error.

## 5. Vercel (frontend)

1. Sign up at https://vercel.com, connect the `solydshopFrontend` GitHub repo.
2. In the project's Environment Variables settings, add:
   - `VITE_BACK_END_URL` = `https://<your-duckdns-domain>`
   - `VITE_FRONTEND_URL` = your Vercel deployment URL
   - `VITE_STRIPE_PUBLISHABLE_KEY` = your production Stripe publishable key
3. Deploy. Every future push to `main` auto-deploys with no extra config.

## 6. Stripe

1. In the Stripe Dashboard, add a webhook endpoint pointing to
   `https://<your-duckdns-domain>/api/payment/webhook`.
2. Copy its signing secret into the VM's `.env` as `STRIPE_WEBHOOK_SECRET`,
   then `docker compose up -d backend` on the VM to pick it up.
3. The local `stripe listen` CLI forwarding is no longer needed once this
   is live.

## 7. Create the first admin account

Roles (ROLE_USER, ROLE_ADMIN, ROLE_SELLER) are seeded automatically in
every environment, including production, since signup and authorization
depend on them existing. Production intentionally has no default admin
account, since demo-account seeding (with a known password) is dev-only.
Sign up normally through the deployed app, then run this on the VM to
promote that account:

```bash
docker compose exec db psql -U <DB_USERNAME> -d solydShopdb -c \
  "UPDATE user_roles SET role_id = (SELECT role_id FROM roles WHERE role_name = 'ROLE_ADMIN') WHERE user_id = (SELECT user_id FROM users WHERE email = 'your-real-email@example.com');"
```

## 8. Backups

On the VM: `crontab -e` and add:
```
0 3 * * * bash /home/ubuntu/solydshop/scripts/backup-db.sh >> /home/ubuntu/solydshop/backups/backup.log 2>&1
```
This keeps the last 7 days of `pg_dump` backups in `~/solydshop/backups/`,
local to the VM (see the design spec's "known gap" note on off-VM
replication).

## 9. Final verification checklist

- [ ] `https://<duckdns-domain>/actuator/health` returns 200 over valid HTTPS
- [ ] Full flow on the live Vercel URL: sign up → log in → browse → add to
      cart → checkout with a Stripe test card → order appears
- [ ] Refresh the page after logging in — session persists (proves
      `SameSite=None; Secure` cookies work cross-site)
- [ ] Push a trivial commit to the backend repo — confirm the GitHub
      Actions workflow runs green and `docker compose ps` on the VM shows
      a new container start time
- [ ] Push a trivial commit to the frontend repo — confirm Vercel
      auto-deploys
