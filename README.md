# FirstClub Membership Service

---

## Product Assumptions & Scope

### What This Service Is Responsible For

The Membership Service is a **Configuration and Entitlement Engine**. It manages *who* is a member, *what plan and tier* they hold, and *what they are entitled to*. It does **not** execute discounts, compute shipping costs, or process payments — those responsibilities belong to downstream services that consume this service's outputs.

---

### Service Roles & Responsibilities

The following table captures the assumed service landscape and how each service interacts with the Membership Service:

| Service | Role | Interaction with Membership |
|---------|------|-----------------------------|
| **Membership (this)** | Owns subscription lifecycle, tier assignments, and benefit configuration | Exposes catalog, subscription CRUD, and benefit lookup APIs |
| **Checkout / Pricing Service** | Applies discounts and resolves shipping costs at order time | Calls `GET /internal/v1/users/{userId}/benefits?type=DISCOUNT` and `?type=SHIPPING` to fetch the member's current entitlements before computing the order total |
| **Order Service** | Processes and records completed orders | Emits an `OrderCompletedEvent` (simulated here via `POST /api/v1/webhooks/orders`) containing `userId`, cumulative `totalOrderCount`, and `orderValue`; Membership Service consumes this to re-evaluate tier eligibility |
| **Payment / Billing Service** | Handles subscription fee collection and renewals | Assumed to exist; this service does not model payment flows. Plan pricing is stored and surfaced but payment initiation and renewal reminders are outside this service's boundary |
| **Identity / User Service** | Manages user accounts and authentication | `userId` is accepted as the `X-User-Id` request header. This service trusts the header — authentication and token validation are assumed to be handled by an upstream API gateway or the Identity Service |
| **Notification Service** | Sends emails/SMS for subscription events | Assumed to exist downstream. This service raises domain events (tier upgrades, cancellations) but does not directly send notifications in the current implementation |

---

### Interaction Flow (Happy Path)

```
User / Frontend
      │
      ▼
API Gateway (auth, rate-limit) ──────────────────────────────────────────┐
      │                                                                    │
      ▼                                                                    │
Membership Service                                                         │
  ├─ GET /api/v1/catalog          → user browses available plans & tiers  │
  ├─ POST /api/v1/subscriptions   → user subscribes (plan + tier)         │
  ├─ GET /api/v1/subscriptions/me → user checks their subscription         │
  ├─ PUT /api/v1/subscriptions/me/modify → tier upgrade / downgrade       │
  └─ PUT /api/v1/subscriptions/me/cancel → cancel subscription            │
                                                                           │
Order Service ──────────────────────────────────────────────────────────  │
  └─ POST /api/v1/webhooks/orders (OrderCompletedEvent)                   │
       └─ Membership re-evaluates tier using Strategy Engine              │
                                                                           │
Checkout / Pricing Service ◄──────────────────────────────────────────────┘
  └─ GET /internal/v1/users/{userId}/benefits?type=SHIPPING
  └─ GET /internal/v1/users/{userId}/benefits?type=DISCOUNT
       └─ Applies entitlements to order total
```

---

### Features & Functionalities Owned by This Service

| Feature | Detail |
|---------|--------|
| **Plan Catalog** | Three plan durations — Monthly (₹99), Quarterly (₹249), Yearly (₹799) — managed via seed data; extensible via DB |
| **Tier Catalog** | Four tiers — Silver, Gold, Platinum (paid tiers) plus a Bronze baseline; each tier has a distinct benefit set |
| **Subscription Lifecycle** | Subscribe, view, upgrade/downgrade tier, cancel; one active subscription per user enforced at the DB level |
| **Benefit Configuration** | Polymorphic benefit configs (`SHIPPING`, `DISCOUNT`, `EXCLUSIVE_DEALS`, `SUPPORT`) stored as typed JSON; consuming services receive strongly-typed DTOs |
| **Tier Evaluation Engine** | Automatic tier upgrades triggered by order events; evaluated by three pluggable strategies (order count, cumulative order value, user cohort prefix) |
| **Internal Benefit API** | Filtered benefit lookup for service-to-service calls; downstream services request only the benefit type they need |
| **Concurrency Safety** | Optimistic locking (`@Version`) prevents lost updates when concurrent requests modify the same subscription |

---

### Features Explicitly Out of Scope

- Payment processing, billing cycles, and invoice generation
- Email / SMS / push notification delivery
- Authentication and JWT issuance (trust the `X-User-Id` header)
- Order processing and inventory management
- Promo code or coupon redemption logic
- Subscription renewal reminders and expiry enforcement (the `end_date` is stored but expiry-triggered auto-cancel is not wired up in this implementation)

---

## Implementation Assumptions

### Data Model

- A user may hold **at most one active subscription** at a time. Uniqueness is enforced by a partial unique index on `(user_id) WHERE status = 'ACTIVE'`.
- Cancellation sets `status = CANCELLED` and records `cancelled_at`. The row is preserved for audit. A cancelled user can re-subscribe.
- `Tier` is a product-level concept (Silver/Gold/Platinum) independent of `Plan` (Monthly/Quarterly/Yearly). A subscription always holds both a plan and a tier.
- Benefit configurations are seeded once and treated as largely static. Runtime changes require a new Flyway migration.

### Tier Evaluation

- Tier evaluation is **event-driven**, triggered only when an `OrderCompletedEvent` is received.
- The engine picks the **highest qualifying tier** across all registered strategies. If no strategy qualifies the user above Silver, the tier remains unchanged.
- The `CohortStrategy` uses `userId` prefix matching (`gold_`, `premium_` → Gold; `vip_`, `platinum_` → Platinum) as a stand-in for a proper cohort service integration.
- Manual tier changes via the modify API coexist with automatic upgrades; a manual downgrade can be overridden by the next order event.

### Security

- Spring Security is configured in **permit-all** mode for this demo. In production, the internal benefit endpoints (`/internal/v1/...`) must be restricted to service-to-service calls via network policy or mutual TLS.
- The `X-User-Id` header is the sole identity signal. An API gateway is assumed to validate the caller's token and inject this header.

### Kafka / Messaging

- The `OrderCompletedEvent` is delivered via a synchronous HTTP webhook in this implementation, simulating what would be a Kafka consumer in production. The payload schema mirrors a real event contract.

### Database

- H2 in-memory database is used for local development and demos. Schema is managed entirely by Flyway migrations (`V1` through `V5`), making a swap to PostgreSQL or MySQL a configuration-only change.

---

## How to Run & Test

### Start the Service

```bash
./gradlew bootRun
```

The service starts on **http://localhost:8080** and seeds all reference data automatically.

### Swagger UI (Interactive API Explorer)

The fastest way to explore and test the API is via Swagger UI:

**http://localhost:8080/swagger-ui.html**

All endpoints are documented with request/response schemas. Use the **"Try it out"** button on any endpoint to execute live requests directly from the browser. For endpoints that require a user identity, add the `X-User-Id` header in the request headers section (e.g., `user1`, `vip_customer`).

The raw OpenAPI spec is available at: **http://localhost:8080/v3/api-docs**

### Other Dev Utilities

| URL | Description |
|-----|-------------|
| http://localhost:8080/swagger-ui.html | Interactive API explorer |
| http://localhost:8080/h2-console | H2 database console (JDBC URL: `jdbc:h2:mem:membershipdb`) |
| http://localhost:8080/actuator/health | Health check |

### Run Tests

```bash
./gradlew test
```

Unit tests cover the `SubscriptionService` business rules and all three `TierEvaluationStrategy` implementations, including edge cases for concurrent modification and tier boundary conditions.

### Quick Demo Sequence (curl)

```bash
# 1. Browse the catalog
curl http://localhost:8080/api/v1/catalog

# 2. Subscribe user1 to Monthly Plan (id=1), Silver tier (id=1)
curl -X POST http://localhost:8080/api/v1/subscriptions \
  -H "Content-Type: application/json" \
  -H "X-User-Id: user1" \
  -d '{"planId": 1, "tierId": 1}'

# 3. Check current subscription
curl -H "X-User-Id: user1" http://localhost:8080/api/v1/subscriptions/me

# 4. Simulate 55 completed orders → triggers Platinum tier upgrade
curl -X POST http://localhost:8080/api/v1/webhooks/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":"user1","orderId":"order-999","orderValue":500,"totalOrderCount":55}'

# 5. Verify tier upgraded to Platinum
curl -H "X-User-Id: user1" http://localhost:8080/api/v1/subscriptions/me

# 6. Fetch Platinum shipping benefits (internal / service-to-service API)
curl "http://localhost:8080/internal/v1/users/user1/benefits?type=SHIPPING"

# 7. Cohort strategy — vip_ prefix resolves to Platinum immediately on next order event
curl -X POST http://localhost:8080/api/v1/subscriptions \
  -H "Content-Type: application/json" \
  -H "X-User-Id: vip_customer" \
  -d '{"planId": 2, "tierId": 1}'

curl -X POST http://localhost:8080/api/v1/webhooks/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":"vip_customer","orderId":"order-vip","orderValue":100,"totalOrderCount":1}'

# 8. Manually upgrade tier — switch user1 from Silver to Gold
curl -X PUT http://localhost:8080/api/v1/subscriptions/me/modify \
  -H "Content-Type: application/json" \
  -H "X-User-Id: user1" \
  -d '{"tierId": 2}'

# 9. Cancel subscription
curl -X PUT http://localhost:8080/api/v1/subscriptions/me/cancel \
  -H "X-User-Id: user1"
```

---

## Production Readiness — What Would Change

The following improvements would be required before deploying this service in a production environment.

### Infrastructure & Database

| Area | Current State | Production Improvement |
|------|--------------|----------------------|
| **Database** | H2 in-memory | Replace with PostgreSQL (or Aurora PostgreSQL for managed HA). Flyway migrations require no changes — only the datasource config changes |
| **Connection Pooling** | Spring Boot default (HikariCP with default settings) | Tune HikariCP pool size based on load (`maximumPoolSize`, `connectionTimeout`, `idleTimeout`); add read replicas for catalog and benefit lookup queries |
| **Schema Migrations** | Flyway on startup | Run Flyway as a pre-deploy job (not on app startup) to separate migration failures from app crashes in a multi-instance deployment |

### Messaging & Event-Driven Architecture

- Replace the HTTP webhook (`POST /api/v1/webhooks/orders`) with a **Kafka consumer** (`@KafkaListener`) subscribed to an `order.completed` topic. The `OrderCompletedEvent` payload contract is already shaped to match a real event.
- Add **dead-letter topic (DLT)** handling so events that fail tier evaluation after retries are parked for manual review rather than silently dropped.
- Publish outbound domain events (tier upgraded, subscription cancelled) to Kafka so downstream services (Notification, Analytics) can react without polling.

### Reliability & Concurrency

- The current optimistic locking retry (3 attempts in `TierEvaluationService`) uses no backoff. In production, replace with **exponential backoff with jitter** to avoid thundering-herd retries under load.
- Add **idempotency keys** on subscription creation to prevent duplicate subscriptions from retried requests (e.g., network timeouts causing a client to re-POST).
- Move tier evaluation to an **async background task** (e.g., via Spring's `@Async` with a bounded `ThreadPoolTaskExecutor`) so a slow evaluation doesn't block the webhook response.

### Security

- Remove the **permit-all Spring Security config** and replace with:
  - JWT validation at the API gateway layer; propagate validated `userId` as a trusted header.
  - mTLS or a shared secret for the `/internal/v1/...` benefit endpoints to restrict them to service-to-service calls only.
- Validate and sanitize the `X-User-Id` header — reject requests with missing or malformed user identifiers rather than passing them through.
- Apply **rate limiting** at the API gateway on subscription creation to prevent abuse.

### Observability

- Add **structured logging** (JSON via Logback + logstash-logback-encoder) with a consistent `userId`, `subscriptionId`, and `traceId` on every log line.
- Expose **Micrometer metrics** (Spring Boot Actuator + Prometheus) for:
  - Subscription creation / cancellation rates
  - Tier upgrade counts by strategy
  - Optimistic lock conflict rates
- Add **distributed tracing** (OpenTelemetry) so cross-service calls (Membership → Order → Checkout) can be traced end-to-end.
- Set up **alerting** on error rates, high conflict rates, and consumer lag on the order events topic.

### Caching

- Cache the catalog (`GET /api/v1/catalog`) and benefit lookups (`GET /internal/v1/users/{userId}/benefits`) with **Redis** (Spring Cache + `@Cacheable`). Catalog data changes rarely; benefit lookups are on the hot path for every checkout.
- Invalidate the user-level benefit cache on tier changes to prevent stale entitlements reaching the Checkout Service.

### Subscription Lifecycle

- Implement **expiry enforcement**: a scheduled job (`@Scheduled` or a dedicated batch service) should transition subscriptions to `EXPIRED` when `end_date` passes and trigger renewal or grace-period flows.
- Add a **grace period** state between active and expired so users with a failed payment renewal aren't immediately stripped of benefits.
- Store a full **audit log** of tier changes (previous tier, new tier, strategy that triggered it, timestamp) for support tooling and billing reconciliation.

### API & Versioning

- Add **pagination** to any list endpoint that could grow (e.g., admin views of subscriptions).
- Enforce **API versioning** at the URL level (`/api/v1/`) — already in place — and document a deprecation policy before introducing `/api/v2/`.
- Add **request timeouts** for all outbound service calls once real HTTP clients replace the simulated webhook.
