# FirstClub Membership Service

A production-grade Spring Boot backend for the FirstClub Membership Program, demonstrating clean architecture, extensible abstractions, and robust concurrency handling.

---

## Quick Start

```bash
./gradlew bootRun
```

The service starts on **http://localhost:8080**. The H2 in-memory database is seeded automatically via Flyway migrations.

| URL | Description |
|-----|-------------|
| http://localhost:8080/swagger-ui.html | Interactive API explorer |
| http://localhost:8080/h2-console | H2 database console (JDBC URL: `jdbc:h2:mem:membershipdb`) |
| http://localhost:8080/actuator/health | Health check |

---

## Run Tests

```bash
./gradlew test
```

---

## Architecture Decisions

### 1. Service Boundaries
The Membership Service is a **Configuration Engine**, not an execution engine.

| Service | Role |
|---------|------|
| Membership (this) | Manages *who* is a member and *what* they are entitled to |
| Checkout / Pricing | Executes discounts and shipping math using configs fetched from this service |
| Order Service | Triggers tier upgrades asynchronously (simulated via webhook) |

### 2. Polymorphic Benefit Contracts (Jackson `@JsonTypeInfo`)

Benefit configurations are stored as raw JSON in the `benefits.config_json` column. At runtime, Jackson deserializes them into strongly-typed subclasses:

```
BenefitConfig (abstract)
├── ShippingBenefitConfig      → type: "SHIPPING"
├── DiscountBenefitConfig      → type: "DISCOUNT"
├── ExclusiveDealsBenefitConfig→ type: "EXCLUSIVE_DEALS"
└── SupportBenefitConfig       → type: "SUPPORT"
```

**Why this matters:** Consuming services receive typed DTOs — no `Map<String, Object>` parsing, no runtime NPEs. Adding a new benefit type requires only a new subclass and a `@JsonSubTypes.Type` annotation entry. Zero changes to the service or evaluation engine.

### 3. Tier Evaluation Engine (Strategy Pattern)

Tier upgrades are evaluated via the `TierEvaluationStrategy` interface. Three implementations are registered as Spring `@Component` beans and auto-discovered:

| Strategy | Rule |
|----------|------|
| `OrderCountStrategy` | orderCount ≥ 3 → Silver, ≥ 10 → Gold, ≥ 50 → Platinum |
| `OrderValueStrategy` | orderValue ≥ ₹1,000 → Silver, ≥ ₹5,000 → Gold, ≥ ₹20,000 → Platinum |
| `CohortStrategy` | userId prefix `gold_`/`premium_` → Gold; `vip_`/`platinum_` → Platinum |

The engine picks the **highest qualifying tier** across all strategies. Adding a new rule requires only a new `@Component` — the engine needs no changes (**Open-Closed Principle**).

### 4. Optimistic Locking for Concurrency

The `Subscription` entity carries a `@Version Long version` field. If two concurrent transactions attempt to modify the same subscription (e.g., user cancels while the system tries a tier upgrade), the **second writer loses** with an `ObjectOptimisticLockingFailureException`.

- **User-facing requests** → Global Exception Handler returns **HTTP 409 Conflict**.
- **System tier upgrades** → `TierEvaluationService` retries up to **3 times** before surfacing the error.

---

## API Reference

### Public APIs

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/catalog` | All plans and tiers |
| `POST` | `/api/v1/subscriptions` | Subscribe (body: `planId`, `tierId`; header: `X-User-Id`) |
| `GET` | `/api/v1/subscriptions/me` | Current subscription |
| `PUT` | `/api/v1/subscriptions/me/cancel` | Cancel subscription |
| `PUT` | `/api/v1/subscriptions/me/modify` | Upgrade / downgrade |

### Internal APIs (Service-to-Service)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/internal/v1/users/{userId}/benefits` | All benefits for user's tier |
| `GET` | `/internal/v1/users/{userId}/benefits?type=SHIPPING` | Shipping rules only |
| `GET` | `/internal/v1/users/{userId}/benefits?type=DISCOUNT` | Discount rules only |

### Webhooks (Kafka simulation)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/webhooks/orders` | Simulates `OrderCompletedEvent` |

---

## Demo Walkthrough

```bash
# 1. Get the catalog
curl http://localhost:8080/api/v1/catalog

# 2. Subscribe user1 to the Monthly Plan (id=1), Silver tier (id=1)
curl -X POST http://localhost:8080/api/v1/subscriptions \
  -H "Content-Type: application/json" \
  -H "X-User-Id: user1" \
  -d '{"planId": 1, "tierId": 1}'

# 3. Check subscription
curl -H "X-User-Id: user1" http://localhost:8080/api/v1/subscriptions/me

# 4. Simulate an order event — 55 orders triggers Platinum upgrade
curl -X POST http://localhost:8080/api/v1/webhooks/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":"user1","orderId":"order-999","orderValue":500,"totalOrderCount":55}'

# 5. Verify tier upgraded to Platinum
curl -H "X-User-Id: user1" http://localhost:8080/api/v1/subscriptions/me

# 6. Fetch Platinum shipping benefits (internal API)
curl "http://localhost:8080/internal/v1/users/user1/benefits?type=SHIPPING"

# 7. Test Cohort Strategy — instant Platinum via userId prefix
curl -X POST http://localhost:8080/api/v1/subscriptions \
  -H "Content-Type: application/json" \
  -H "X-User-Id: vip_customer" \
  -d '{"planId": 2, "tierId": 1}'

curl -X POST http://localhost:8080/api/v1/webhooks/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":"vip_customer","orderId":"order-vip","orderValue":100,"totalOrderCount":1}'

# 8. Upgrade plan — switch from Silver to Gold tier manually
curl -X PUT http://localhost:8080/api/v1/subscriptions/me/modify \
  -H "Content-Type: application/json" \
  -H "X-User-Id: user1" \
  -d '{"tierId": 2}'

# 9. Cancel subscription
curl -X PUT http://localhost:8080/api/v1/subscriptions/me/cancel \
  -H "X-User-Id: user1"
```

---

## Project Structure

```
src/main/java/com/firstclub/membership/
├── config/           SecurityConfig, OpenApiConfig
├── controller/       CatalogController, SubscriptionController,
│                     BenefitController, WebhookController
├── domain/
│   ├── entity/       Plan, Tier, Benefit, Subscription
│   └── enums/        PlanDuration, TierLevel, BenefitType, SubscriptionStatus
├── dto/
│   ├── benefit/      BenefitConfig (abstract), Shipping/Discount/ExclusiveDeals/SupportBenefitConfig
│   ├── request/      CreateSubscriptionRequest, ModifySubscriptionRequest, OrderCompletedEvent
│   └── response/     ApiResponse, CatalogResponse, PlanResponse, TierResponse,
│                     SubscriptionResponse, BenefitResponse
├── exception/        GlobalExceptionHandler, *NotFoundException, AlreadySubscribedException
├── repository/       PlanRepository, TierRepository, BenefitRepository, SubscriptionRepository
├── service/          CatalogService, SubscriptionService, BenefitService, TierEvaluationService
└── strategy/         TierEvaluationStrategy, OrderCountStrategy, OrderValueStrategy, CohortStrategy

src/main/resources/
├── application.properties
└── db/migration/
    ├── V1__init_schema.sql
    └── V2__seed_data.sql
```

---

## Technology Stack

| Technology | Version | Purpose |
|-----------|---------|---------|
| Spring Boot | 3.4.5 | Application framework |
| Spring Data JPA | managed | ORM + Optimistic Locking |
| Flyway | managed | Schema versioning |
| H2 | managed | In-memory database (demo) |
| Hibernate Validator | managed | Bean validation |
| Spring Security | managed | Permit-all config (demo) |
| Springdoc OpenAPI | 2.8.8 | Swagger UI |
| Lombok | managed | Boilerplate reduction |
| JUnit 5 + Mockito | managed | Unit testing |
