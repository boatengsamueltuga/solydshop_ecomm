# Deployment Runbook

One-time setup to get SolydShop running in production for free. Code-level
changes referenced here are already in place (see
`docs/superpowers/specs/2026-07-04-free-deployment-design.md` in the
frontend repo for the full design).

## 1. DigitalOcean Droplet

1. Sign up at https://www.digitalocean.com.
2. Create a Droplet: Ubuntu 24.04 (LTS) x64, Basic plan, Regular (shared
   CPU) — 2 GB RAM / 1 vCPU (~$12/mo) is a safe minimum for Postgres +
   Spring Boot + Caddy running together; the 1 GB/$6 plan can work too if
   you add a swap file, but expect the JVM to be tight on memory. Choose a
   datacenter region close to your users.
3. Add your SSH key during creation (DigitalOcean's droplet creation flow
   installs it for you — no separate key-download step like Oracle's).
   The droplet's public IP is static by default; no extra "reserve a
   static IP" step is needed unless you want a Floating IP you can
   re-point between droplets later.
4. Under Networking → Firewalls, create a firewall (or reuse the
   droplet's default) allowing inbound TCP 22, 80, and 443 from
   `0.0.0.0/0`, and apply it to the droplet.
5. Note the droplet's public IP. You log in as `root` by default (or the
   sudo user you configured at creation).

## 2. DuckDNS

1. Sign up at https://www.duckdns.org (GitHub/Google login).
2. Create a subdomain (e.g. `solydshop.duckdns.org`) and point it at the
   VM's static public IP from step 1.

## 3. VM bootstrap

1. SSH into the VM: `ssh -i <key> root@<vm-ip>` (or your configured sudo
   user instead of `root`)
2. Copy `scripts/vm-bootstrap.sh` to the VM and run it: `bash vm-bootstrap.sh`
3. If you're using a non-root sudo user, log out and back in (for the
   `docker` group membership to apply). Not needed if you're `root`.
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
- `VM_HOST` — the droplet's public IP or DuckDNS hostname
- `VM_USER` — `root` (or your configured sudo user)
- `VM_SSH_KEY` — a **dedicated** deploy keypair's private key (don't reuse
  your personal key): generate with `ssh-keygen -t ed25519 -f deploy_key`,
  add `deploy_key.pub` to the droplet's `~/.ssh/authorized_keys`, paste the
  contents of `deploy_key` (private half) as this secret.

By default, GHCR packages are private — after the first push from CI, go to
the package's settings on GitHub and make it public (simplest option,
since the image itself contains no secrets — those are injected via `.env`
at container runtime). Otherwise the VM's `docker compose pull` will fail
with an authentication error.

Every image is tagged both `:latest` and with the commit SHA, so there's
always a rollback path. To roll back, on the VM run `docker compose pull`
after changing the image tag in `docker-compose.yml` to a previous commit
SHA (visible in the GHCR package's version history or GitHub Actions run
history), or manually `docker pull ghcr.io/<owner>/solydshop-backend:<previous-sha> && docker tag ... :latest && docker compose up -d backend`.

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

On the VM: `crontab -e` and add, substituting your actual home directory
(e.g. `/root` if logged in as root, `/home/<user>` otherwise):
```
0 3 * * * bash /root/solydshop/scripts/backup-db.sh >> /root/solydshop/backups/backup.log 2>&1
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
