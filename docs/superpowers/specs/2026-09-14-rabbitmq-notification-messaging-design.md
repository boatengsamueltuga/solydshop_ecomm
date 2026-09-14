# Design: RabbitMQ for Notification Creation (Microservices Learning — Phase 3)

**Status:** Approved, implementation in progress
**Date:** 2026-09-14

## Context

Third step of the incremental monolith-to-microservices learning project. Phase 1 (monolith on
k3s) and Phase 2 (Notification service extracted, communicating over plain REST) are both done
and verified, on the cluster and in production. This phase swaps the *creation* path from a
synchronous REST call to an asynchronous RabbitMQ message — the deliberate "messaging" lesson
that was scoped out of Phase 2 on purpose.

**Roadmap recap:**
1. Phase 1 — done: monolith on k3s.
2. Phase 2 — done: Notification service extracted, REST-based.
3. **Phase 3 (this doc):** swap notification *creation* to RabbitMQ.
4. Phase 4: migrate from k3s to DOKS.
5. Later: further service extractions.

## Decisions

| Decision | Choice | Why |
|---|---|---|
| Scope | Only notification **creation** moves to messaging | It's a fire-and-forget event. Reads (list, unread count, mark-read, delete) need an immediate answer for the frontend and stay as REST — messaging isn't the right tool for request/response. |
| Broker host | Self-hosted RabbitMQ on the same k3s cluster | Continues what's already running; running a stateful broker on Kubernetes is its own useful lesson (a managed broker was the alternative, deliberately not chosen here). |
| Topology | One durable direct exchange (`notifications.exchange`) → one durable queue (`notifications.create.queue`), routing key `notification.created` | Simplest topology that still teaches the real publish/consume/ack cycle. Multiple queues / other exchange types are a good later lesson once this works. |
| Failure handling | No dead-lettering in this pass — a message that fails processing is logged and acknowledged (dropped), not requeued forever | Keeps this phase focused on the core mechanism. A poison-message infinite-retry loop is worse than losing one notification. Dead-lettering is tracked as a follow-up issue, not silently skipped. |
| Storage | RabbitMQ gets a PersistentVolumeClaim for its data directory | Needed for message durability to survive a pod restart. This is the deferred "stateful workload on k8s" lesson from Phase 1 (where Postgres was deliberately kept outside the cluster). |
| Management UI | Enabled, exposed via NodePort (`30672`) alongside the existing backend NodePort | Lets you visually inspect exchanges/queues/messages in a browser — genuinely useful for learning, not just a diagnostic afterthought. Protected by non-default credentials; this is still a learning cluster with no real customer data, same trade-off already accepted for the backend's NodePort in Phase 1. |
| Client library | Spring AMQP (`spring-boot-starter-amqp`) in both repos | Standard, idiomatic Spring Boot choice — no reason to reach for a raw AMQP client. |

## What this phase does NOT include

Dead-letter queues/retry policies, multiple queues or exchange types (fanout/topic), moving any
read operation to messaging, RabbitMQ clustering/HA. Each is a reasonable future lesson, not
silently forgotten — dead-lettering specifically gets tracked as a GitHub issue once this phase
ships.

## Component changes

**`solydshop_ecomm` (monolith):**
- Add `spring-boot-starter-amqp` dependency.
- `NotificationServiceImpl` becomes a hybrid: `createForUser(...)` (both overloads) publishes a
  message via `RabbitTemplate` instead of an HTTP POST. Every other method keeps using the
  existing `RestClient` unchanged.
- `NotificationController` and all 5 callers (Order/Product/Quote/SellerApplication/
  SellerDowngrade services) require zero changes — same interface as Phase 2.
- New config: RabbitMQ connection properties (host/port/username/password), exchange/queue/
  routing-key names.
- Error handling: publish failures are caught and logged, never propagated — a broker outage must
  not break placing an order, same principle as the Phase 2 HTTP client.

**`solydshop-notifications`:**
- Add `spring-boot-starter-amqp` dependency.
- New `@RabbitListener` component consuming `notifications.create.queue`, deserializing into the
  same shape as the existing `CreateNotificationRequest`, and calling the *existing*
  `NotificationService.createForUser(...)` — reusing the logic already there, not duplicating it.
- The existing `POST /internal/notifications` REST endpoint stays as-is (harmless to keep; also
  useful for the kind of direct smoke-testing we did in Phase 2).

**New k8s resources (both repos' `k8s/` folders get relevant manifests, RabbitMQ's own live in
`solydshop_ecomm/k8s/` since it's shared infrastructure, not owned by either service):**
- `rabbitmq-secret.yaml` (gitignored) — `RABBITMQ_DEFAULT_USER`/`RABBITMQ_DEFAULT_PASS`.
- `rabbitmq-pvc.yaml` — PersistentVolumeClaim for `/var/lib/rabbitmq`.
- `rabbitmq-deployment.yaml` — image `rabbitmq:4-management-alpine`.
- `rabbitmq-service.yaml` — ClusterIP for AMQP (5672, internal-only), NodePort for the management
  UI (15672 → 30672).

## Testing

- Monolith: unit test that `createForUser` publishes via the template and that a broker failure
  doesn't throw (same pattern as the Phase 2 `NotificationServiceImplTest`, pointed at a
  publish failure instead of a connection failure).
- Notification service: unit test that the listener, given a message, calls
  `NotificationService.createForUser(...)` with the right arguments.
- End-to-end: trigger a real notification-creating action, watch the message appear and drain in
  the RabbitMQ management UI, confirm it lands in the notification service's database — same
  verification shape as Phase 2, extended to show the message actually flowing through the queue.
