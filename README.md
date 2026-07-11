<div align="center">

# SolydShop — Backend

**The Spring Boot REST API powering SolydShop** — a full-stack e-commerce platform for heavy
equipment parts & supplies, serving three distinct roles: buyers, sellers, and admins.

[![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=fff)](https://openjdk.org)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?logo=springboot&logoColor=fff)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=fff)](https://www.postgresql.org)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=fff)](https://www.docker.com)
[![Stripe](https://img.shields.io/badge/Stripe-Payments-635BFF?logo=stripe&logoColor=fff)](https://stripe.com)
[![Cloudinary](https://img.shields.io/badge/Cloudinary-Images-3448C5?logo=cloudinary&logoColor=fff)](https://cloudinary.com)
[![Deployed on DigitalOcean](https://img.shields.io/badge/Deployed_on-DigitalOcean-0080FF?logo=digitalocean&logoColor=fff)](https://solydshop.vercel.app)

[**Live Demo**](https://solydshop.vercel.app) · [Frontend Repo](https://github.com/boatengsamueltuga/solydshopFrontend) · [Report a Bug](https://github.com/boatengsamueltuga/solydshop_ecomm/issues)

</div>

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [API Overview](#api-overview)
- [Roles](#roles)
- [Testing](#testing)
- [Deployment](#deployment)
- [Related](#related)
- [Author](#author)

## Overview

SolydShop is a full-stack e-commerce platform for sourcing heavy equipment parts (excavators,
bulldozers, cranes, and more). This repo is the **backend**: a Spring Boot REST API that handles
auth, catalog, cart/checkout, orders, payments, and the seller/admin workflows behind the
[solydshopFrontend](https://github.com/boatengsamueltuga/solydshopFrontend) React app.

It's built around three roles with dedicated permission boundaries — **buyers**, **sellers**, and
**admins** — enforced with Spring Security + JWT, not just hidden in the UI.

## Features

**Core Commerce**
- Product catalog with search, category/price filtering, and moderation states
  (pending review → active → suspended/archived)
- Cart, wishlist, and order management with denormalized order-item snapshots (orders survive
  later product edits/deletes)
- Reviews (one per user per product, enforced at the database level)
- B2B quote-request workflow between buyers and sellers/admins

**Payments**
- Stripe PaymentIntent–first checkout: the cart total is locked and a PaymentIntent is created
  *before* the order row, with pessimistic row locking to prevent overselling under concurrent
  checkouts
- Webhook-driven reconciliation (`payment_intent.succeeded` / `payment_intent.payment_failed`),
  idempotent on both paths
- Admin-only refund-and-cancel flow — the only sanctioned way to cancel a paid order

**Auth & Security**
- JWT via HTTP-only cookies (`accessToken` / `refreshToken`) with rotation on refresh
- CSRF protection (`XSRF-TOKEN` cookie + header), with a JSON fallback endpoint for cross-domain
  deployments where the frontend can't read a cross-origin cookie
- Account lockout after repeated failed logins, rate-limited password reset with audit logging
- Role-based access control (`ROLE_USER`, `ROLE_SELLER`, `ROLE_ADMIN`) via Spring Security +
  method-level `@PreAuthorize`

**Seller & Admin Workflows**
- Seller onboarding and seller→buyer downgrade requests, both gated by admin approval
- Full product moderation queue (approve / reject / suspend / reinstate / archive)
- User management (roles, account unlock), category management, order overrides
- In-app notifications dispatched on key events (new orders, moderation decisions, approvals)

**Integrations**
- Cloudinary for product image uploads
- Spring Mail (SMTP) for password reset and account emails, sent as branded HTML with the
  SolydShop logo embedded inline

## Tech Stack

- **Language / Runtime:** Java 17
- **Framework:** Spring Boot 3.5 (Web, Security, Data JPA, Validation, Mail, Actuator)
- **Database:** PostgreSQL
- **Auth:** JWT (jjwt) via HTTP-only cookies, Spring Security, CSRF protection
- **Payments:** Stripe Java SDK (PaymentIntents + webhooks)
- **Images:** Cloudinary
- **Email:** Spring Mail (SMTP)
- **Build:** Maven (`mvnw`)
- **CI/CD:** GitHub Actions (test → build → push image → deploy)
- **Deployment:** Docker Compose (db / backend / Caddy reverse proxy) on a DigitalOcean droplet

## Architecture

```
┌──────────────┐        ┌──────────────────┐   JDBC   ┌──────────────┐
│   React SPA  │  REST  │  Spring Boot API │ ───────► │  PostgreSQL  │
│ (Vercel)     │ ─────► │  (this repo)     │          └──────────────┘
└──────────────┘        │  DigitalOcean +  │ ───────► Stripe (PaymentIntents + webhooks)
                         │  Caddy (TLS)     │ ───────► Cloudinary (image uploads)
                         └──────────────────┘ ───────► SMTP (transactional email)
```

Deployed via Docker Compose behind Caddy (automatic TLS via DuckDNS), with images built and
pushed to GHCR by GitHub Actions on every merge to `main`.

## Project Structure

```
src/main/java/com/solydshop/ecommerce/
├── config/         # CORS/security config, Cloudinary bean, role/data seeders, schema patch runner
├── controller/     # REST controllers (Auth, Cart, Category, Notification, Order, Payment,
│                   #   Product, Quote, Review, SellerApplication, SellerDowngrade, Upload, User, Wishlist)
├── entity/         # JPA entities (User, Product, Category, Cart, Order, Wishlist, Review,
│                   #   QuoteRequest, SellerApplication, SellerDowngradeRequest, Notification, ...)
├── exception/      # Global exception handler, custom exceptions
├── payload/        # Request/response DTOs
├── repository/     # Spring Data JPA repositories
├── security/       # JWT filter, JWT/cookie utils, security config
├── service/        # Business logic (one interface + Impl per aggregate) + Cloudinary/Email/
│                   #   RateLimit/RefreshToken/AuditLog services
└── util/           # Shared constants (pagination/sort defaults)
```

## Getting Started

### Prerequisites

- Java 17
- PostgreSQL 16 (local instance, or via Docker)
- Maven Wrapper (bundled — no separate Maven install needed)

### Setup

1. Clone the repo and copy the properties template:
   ```bash
   git clone https://github.com/boatengsamueltuga/solydshop_ecomm.git
   cd solydshop_ecomm
   cp src/main/resources/application.properties.example src/main/resources/application.properties
   ```
2. Fill in your local values (see [Environment Variables](#environment-variables) below).
3. Create the database:
   ```sql
   CREATE DATABASE solydShopdb;
   ```
4. Run the app:
   ```bash
   ./mvnw spring-boot:run
   ```
   The API starts on `http://localhost:8080`.

## Environment Variables

| Variable | Purpose |
|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL connection (default `jdbc:postgresql://localhost:5432/solydShopdb`) |
| `JWT_SECRET`, `JWT_EXPIRATION` | JWT signing secret and access-token expiry (ms) |
| `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` | Image upload storage |
| `STRIPE_SECRET_KEY`, `STRIPE_PUBLISHABLE_KEY`, `STRIPE_WEBHOOK_SECRET` | Payment processing |
| `MAIL_USERNAME`, `MAIL_PASSWORD` | SMTP credentials for password reset / notification emails |
| `FRONTEND_URL` | Frontend origin, used to build links in outgoing emails |
| `CORS_ALLOWED_ORIGINS` | Comma-separated list of allowed CORS origins |
| `COOKIE_SECURE`, `COOKIE_SAME_SITE` | Auth cookie flags (must be `Secure` if `SameSite=None`) |

## API Overview

All endpoints are prefixed `/api`, grouped by access level:

| Access | Endpoints |
|---|---|
| **Public** | `/api/auth/**` (login, register, refresh, logout, password reset, CSRF), `/api/public/**` (browse products/categories, reviews) |
| **Authenticated** | `/api/cart/**`, `/api/order/**`, `/api/payment/**`, `/api/wishlist/**`, `/api/notifications/**`, `/api/reviews/**`, `/api/quotes/**`, `/api/seller-applications/**` |
| **Seller** (`ROLE_SELLER`) | `/api/seller/**` — product management, seller orders/quotes |
| **Admin** (`ROLE_ADMIN`) | `/api/admin/**` — users, categories, product moderation, orders, seller applications, quotes |

## Roles

| Role | Access |
|---|---|
| `ROLE_USER` | Browse, cart, checkout, orders, reviews, wishlist, quote requests |
| `ROLE_SELLER` | Everything above + product management, seller dashboard, quote responses |
| `ROLE_ADMIN` | Full access: user management, product moderation, order management, analytics |

## Testing

```bash
./mvnw test
```

Covers security filters/config, exception handling, role seeding, and core service logic
(orders, products, seller applications/downgrades). CI runs the full suite against a real
Postgres service container on every push.

## Deployment

See [DEPLOYMENT.md](DEPLOYMENT.md) for the complete production deployment runbook
(DigitalOcean droplet, DuckDNS, Caddy auto-TLS, Vercel frontend, GitHub Actions CI/CD,
database backups).

## Related

- **Frontend:** [solydshopFrontend](https://github.com/boatengsamueltuga/solydshopFrontend) — React 19 / Vite 8 SPA
- **Live site:** [solydshop.vercel.app](https://solydshop.vercel.app)

## Author

**Samuel Nketiah Boateng**
Full-Stack / Java Backend Developer

[![GitHub](https://img.shields.io/badge/GitHub-boatengsamueltuga-181717?logo=github&logoColor=fff)](https://github.com/boatengsamueltuga)
[![Email](https://img.shields.io/badge/Email-boatengsamuel237%40gmail.com-D14836?logo=gmail&logoColor=fff)](mailto:boatengsamuel237@gmail.com)
