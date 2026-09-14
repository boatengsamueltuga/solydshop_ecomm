# Design: Deploy the Monolith to k3s (Microservices Learning — Phase 1)

**Status:** Approved, ready to implement
**Date:** 2026-09-09

## Context

This is the first step of a longer, deliberately incremental project to
learn microservices by evolving SolydShop's backend — driven by wanting
hands-on experience with the concepts, not by a scaling/ops pain point in
the current app.

**Full roadmap** (only Phase 1 is in scope for this doc — each later phase
gets its own design when we get there):

1. **Phase 1 (this doc):** Deploy the current monolith, unchanged, onto a
   self-hosted k3s cluster. Pure Kubernetes fundamentals — no
   microservices yet.
2. Phase 2: Extract a Notification service (own repo, own database, plain
   REST call from the monolith) — deployed onto the same k3s cluster.
3. Phase 3: Swap that REST call for RabbitMQ (async messaging).
4. Phase 4: Migrate from self-hosted k3s to managed DigitalOcean
   Kubernetes (DOKS), as a point of comparison.
5. Later: extract further services (e.g. Catalog) one at a time.

k3s was chosen over a managed cluster for this phase specifically because
the goal is to learn how Kubernetes *works*, not just how to consume it —
that means installing and running the control plane yourself. DOKS
(managed) is the deliberate follow-up in Phase 4.

## Decisions

| Decision | Choice | Why |
|---|---|---|
| Environment | A **new, separate** droplet | Production (current droplet, serving `solydshop.vercel.app`) is untouched — zero risk to the live site while learning. |
| Cluster topology | **Single-node** k3s (one droplet is both control-plane and worker) | Cheapest, simplest, and still exercises every core K8s object. Multi-node scheduling is a later lesson if wanted. |
| Postgres placement | **Outside** the cluster — installed natively on the same droplet | Keeps this phase focused on Deployments/Services/ConfigMaps/Secrets, without also learning StatefulSets/PVCs/storage classes on day one. |
| Data | **Fresh, empty database** | This cluster never touches production data. Roles/seed data are created automatically on first boot (`RoleSeeder`/`SchemaMigrationRunner`, same as any fresh environment). |
| Networking/access | **Raw IP + NodePort**, no domain or TLS | One less moving part while learning core objects. A domain + Ingress + cert-manager (TLS) is a good deliberate follow-up once this works. |
| App code/image | **Unchanged** — reuse the existing `ghcr.io/boatengsamueltuga/solydshop-backend` image built by the existing CI pipeline | This phase is entirely about *how you run* the app, not the app itself. |

## What this phase does NOT include

Deliberately deferred to later, separate steps: Ingress/TLS, a domain
name, in-cluster Postgres, multi-node scaling, RabbitMQ, DOKS, and any
new services. Don't add these while following the steps below — each is
its own future lesson.

## Prerequisites

- A DigitalOcean account (same one used for production is fine — this is
  just a second, independent droplet on it)
- An SSH key already added to your DigitalOcean account (same one you use
  for the production droplet works)
- `kubectl` installed locally — you already have one at
  `C:\Program Files\Docker\Docker\resources\bin\kubectl.exe` (bundled with
  Docker Desktop), which is all we need
- `ssh` and `scp` available locally (you have OpenSSH already)

---

## Steps

### 1. Create the droplet

In the DigitalOcean console → **Create → Droplets**:

- **Image:** Ubuntu 24.04 (LTS) x64
- **Plan:** Basic, Regular (shared CPU), **2 GB RAM / 1 vCPU** (~$12/mo).
  k3s plus the Spring Boot app plus Postgres is workable in 1 GB but
  tight; 2 GB avoids fighting memory pressure while you're learning.
- **Authentication:** SSH key — select your existing key
- **Hostname:** something like `solydshop-k3s-learn` so it's obviously
  separate from production in your droplet list

Create it, then note its public IP (call it `<K3S_IP>` below).

Under **Networking → Firewalls**, create a new firewall (don't reuse
production's) with inbound rules:
- TCP `22` (SSH) from `0.0.0.0/0`
- TCP `6443` (Kubernetes API server) from `0.0.0.0/0`
- TCP `30080` (our app's NodePort, chosen below) from `0.0.0.0/0`

Apply it to the new droplet only.

> This cluster has no real customer data, so opening 6443/30080 broadly
> is an acceptable trade for simplicity while learning. Don't reuse this
> firewall for anything holding real data later.

### 2. Install k3s

SSH in:

```bash
ssh root@<K3S_IP>
```

Install k3s:

```bash
curl -sfL https://get.k3s.io | sh -
```

**Expected output:** a series of `[INFO]` lines ending with something
like `[INFO]  systemd: Starting k3s`. This takes under a minute.

Verify the node is up:

```bash
sudo k3s kubectl get nodes
```

**Expected output:** one row, your hostname, `STATUS` = `Ready` (may show
`NotReady` for the first ~30–60 seconds — re-run until it flips).

### 3. Pull the kubeconfig to your local machine

Still on your local machine:

```bash
scp root@<K3S_IP>:/etc/rancher/k3s/k3s.yaml ./k3s-learn.yaml
```

**Expected output:** a progress line ending `100%` — a small YAML file is
now in your current directory.

Edit `k3s-learn.yaml` and change the `server:` line from
`https://127.0.0.1:6443` to `https://<K3S_IP>:6443`.

Point `kubectl` at it for this session:

```bash
export KUBECONFIG=$(pwd)/k3s-learn.yaml    # bash
# or, PowerShell:
$env:KUBECONFIG = "$(Get-Location)\k3s-learn.yaml"
```

Verify from your own machine now:

```bash
kubectl get nodes
```

**Expected output:** same single `Ready` node as step 2, but this time
you ran it locally, not over SSH — confirms your local `kubectl` now
controls the remote cluster.

### 4. Install Postgres on the droplet (outside the cluster)

Back on the droplet (`ssh root@<K3S_IP>`):

```bash
apt update && apt install -y postgresql
```

Create the database and a dedicated user:

```bash
sudo -u postgres psql -c "CREATE DATABASE \"solydShopdb\";"
sudo -u postgres psql -c "CREATE USER solydshop WITH PASSWORD 'CHOOSE-A-STRONG-PASSWORD';"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE \"solydShopdb\" TO solydshop;"
```

Let pods reach Postgres. k3s's default pod network is `10.42.0.0/16`.
Find your Postgres config directory (usually `/etc/postgresql/16/main/`):

```bash
PGDIR=$(dirname $(sudo -u postgres psql -tAc "SHOW config_file;"))
echo $PGDIR
```

Edit `$PGDIR/postgresql.conf`, set:

```
listen_addresses = '*'
```

Edit `$PGDIR/pg_hba.conf`, add this line (allows only the pod network,
password-authenticated):

```
host    solydShopdb     solydshop       10.42.0.0/16            scram-sha-256
```

Restart Postgres:

```bash
systemctl restart postgresql
```

Check `ufw` isn't blocking pod traffic (it's usually inactive by default
on DO droplets, but confirm):

```bash
ufw status
```

**Expected output:** `Status: inactive`. If it says `active`, run
`ufw allow from 10.42.0.0/16 to any port 5432`.

Find the droplet's private-network-facing IP Postgres is now listening
on (for a single-node droplet, its main private/public interface IP
works — call it `<DB_HOST_IP>`, find it with `hostname -I`, use the
first address shown).

### 5. Write the Kubernetes manifests

Back on your local machine, create a new folder for these (e.g.
`k3s-learn/` inside `solydshop_ecomm`, or anywhere you like — they're not
part of the app's build).

**`secret.yaml`** — fill in real values, don't commit this file:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: solydshop-secrets
type: Opaque
stringData:
  DB_USERNAME: solydshop
  DB_PASSWORD: "CHOOSE-A-STRONG-PASSWORD"   # same as step 4
  JWT_SECRET: "generate-a-long-random-string"
  CLOUDINARY_API_KEY: "your-key-or-a-throwaway-test-account"
  CLOUDINARY_API_SECRET: "your-secret"
  STRIPE_SECRET_KEY: "sk_test_..."           # test-mode key recommended
  STRIPE_WEBHOOK_SECRET: "whsec_test_..."
  MAIL_USERNAME: "your-email@gmail.com"
  MAIL_PASSWORD: "your-app-password"
```

**`configmap.yaml`**:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: solydshop-config
data:
  DB_URL: "jdbc:postgresql://<DB_HOST_IP>:5432/solydShopdb"
  CLOUDINARY_CLOUD_NAME: "your-cloud-name"
  STRIPE_PUBLISHABLE_KEY: "pk_test_..."
  JWT_EXPIRATION: "3600000"
  FRONTEND_URL: "http://localhost:3000"
  CORS_ALLOWED_ORIGINS: "http://localhost:3000"
  COOKIE_SECURE: "false"
  COOKIE_SAME_SITE: "Lax"
  SPRING_PROFILES_ACTIVE: "prod"
```

> `COOKIE_SECURE=false` / `SAME_SITE=Lax` because we're testing over
> plain HTTP with no domain yet — this matches local dev, not production.
> Revisit once TLS is added in a later phase.

**`deployment.yaml`**:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: solydshop-backend
spec:
  replicas: 1
  selector:
    matchLabels:
      app: solydshop-backend
  template:
    metadata:
      labels:
        app: solydshop-backend
    spec:
      containers:
        - name: backend
          image: ghcr.io/boatengsamueltuga/solydshop-backend:latest
          ports:
            - containerPort: 8080
          envFrom:
            - configMapRef:
                name: solydshop-config
            - secretRef:
                name: solydshop-secrets
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 45
            periodSeconds: 15
```

**`service.yaml`**:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: solydshop-backend
spec:
  type: NodePort
  selector:
    app: solydshop-backend
  ports:
    - port: 8080
      targetPort: 8080
      nodePort: 30080
```

Since the GHCR package for `solydshop-backend` is already public (per
`DEPLOYMENT.md`), no image pull secret is needed.

### 6. Apply and verify

```bash
kubectl apply -f secret.yaml -f configmap.yaml -f deployment.yaml -f service.yaml
```

**Expected output:** four lines, each ending `created`.

Watch the pod come up:

```bash
kubectl get pods -w
```

**Expected output:** one pod, `solydshop-backend-...`, moving from
`Pending` → `ContainerCreating` → `Running`, with `READY` becoming `1/1`
after the readiness probe passes (allow the ~30s `initialDelaySeconds`).
Ctrl+C once it's `1/1 Running`.

If it doesn't reach `Running`, check logs:

```bash
kubectl logs deploy/solydshop-backend
```

Common first-run issues: wrong `DB_URL`/`DB_HOST_IP`, Postgres not
accepting the pod's connection (re-check step 4's `pg_hba.conf` line and
that `systemctl restart postgresql` actually ran).

### 7. Verify the app end-to-end

From your own machine:

```bash
curl http://<K3S_IP>:30080/actuator/health
```

**Expected output:** `{"status":"UP"}`.

Then exercise real endpoints to confirm the full app works against the
fresh database — e.g. with `curl` or Postman:
- `POST http://<K3S_IP>:30080/api/auth/register` — create an account
- `POST http://<K3S_IP>:30080/api/auth/login` — log in
- `GET http://<K3S_IP>:30080/api/public/products` — should return an
  empty list (fresh DB, no products yet) with a 200, not an error

If all three work, Phase 1 is done: the unmodified monolith is running
on a Kubernetes cluster you installed and configured yourself, completely
isolated from production.

---

## Rollback / safety

Nothing here touches production. If anything goes wrong, the fix is
always available: destroy the learning droplet in the DigitalOcean
console and start again from step 1 — there's no data or traffic on it
that matters.

## Next step

Once this is verified, Phase 2 (extracting the Notification service) is
a separate design to brainstorm when you're ready for it.
