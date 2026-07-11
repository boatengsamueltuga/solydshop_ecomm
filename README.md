# SolydShop — Backend (solydshop_ecomm)

Spring Boot REST API powering [SolydShop](https://solydshop.vercel.app), a full-stack e-commerce
platform for buyers, sellers, and admins. Pairs with the
[solydshopFrontend](https://github.com/boatengsamueltuga/solydshopFrontend) React app.

**Live API:** deployed on a DigitalOcean droplet behind Caddy — see [DEPLOYMENT.md](DEPLOYMENT.md)
for the full production runbook.

## Tech Stack

- **Language / Runtime:** Java 17
- **Framework:** Spring Boot 3.5 (Web, Security, Data JPA, Validation, Mail, Actuator)
- **Database:** PostgreSQL
- **Auth:** JWT via HTTP-only cookies (`accessToken` / `refreshToken`), Spring Security, CSRF
  (`XSRF-TOKEN` cookie), role-based access control (`ROLE_USER`, `ROLE_SELLER`, `ROLE_ADMIN`)
- **Payments:** Stripe (PaymentIntent checkout + webhook reconciliation)
- **Images:** Cloudinary
- **Email:** Spring Mail (SMTP)
- **Build:** Maven (`mvnw`)
- **Deployment:** Docker Compose (db / backend / Caddy reverse proxy) on DigitalOcean,
  images built and pushed via GitHub Actions CI/CD

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

### Running Tests

```bash
./mvnw test
```

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

All endpoints are prefixed `/api`. Grouped by access level:

- **Public:** `/api/auth/**` (login, register, refresh, logout, password reset, CSRF), `/api/public/**` (browse products/categories, reviews)
- **Authenticated:** `/api/cart/**`, `/api/order/**`, `/api/payment/**`, `/api/wishlist/**`, `/api/notifications/**`, `/api/reviews/**`, `/api/quotes/**`, `/api/seller-applications/**`
- **Seller only (`ROLE_SELLER`):** `/api/seller/**` (product management, seller orders/quotes)
- **Admin only (`ROLE_ADMIN`):** `/api/admin/**` (users, categories, product moderation, orders, seller applications, quotes)

## Roles

| Role | Access |
|---|---|
| `ROLE_USER` | Browse, cart, checkout, orders, reviews, wishlist, quote requests |
| `ROLE_SELLER` | Everything above + product management, seller dashboard, quote responses |
| `ROLE_ADMIN` | Full access: user management, product moderation, order management, analytics |

## Deployment

See [DEPLOYMENT.md](DEPLOYMENT.md) for the complete production deployment runbook
(DigitalOcean droplet, DuckDNS, Caddy auto-TLS, Vercel frontend, GitHub Actions CI/CD,
database backups).

## Related

- Frontend: [solydshopFrontend](https://github.com/boatengsamueltuga/solydshopFrontend)
- Live site: [solydshop.vercel.app](https://solydshop.vercel.app)
