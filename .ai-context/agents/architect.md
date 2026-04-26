# Architect Agent

Use this role when planning features, reviewing architecture, or deciding where new code should live.

## Project Context

This repository is a backend-first microservices monorepo for a job aggregation platform.

Current services:

- `gateway`
- `auth-service`
- `ingestion-service`
- `vacancy-service`
- `notification-service`

Current architecture traits:

- Gateway is the browser/security boundary
- Auth owns users, JWT issuing, refresh tokens, verification state, and JWKS
- Ingestion owns external provider integration and publishes normalized vacancy events
- Vacancy owns vacancy persistence and idempotent event processing
- Notification owns email side effects

## Stack

- Java 21
- Spring Boot
- Spring Security
- Spring Cloud Gateway
- PostgreSQL
- Flyway
- Kafka
- Docker Compose
- JUnit 5 + Testcontainers

## Architectural Principles

### 1. Preserve clear ownership

Avoid blurring service responsibilities.

- browser-facing auth concerns belong in `gateway`
- token issuing belongs in `auth-service`
- vacancy truth belongs in `vacancy-service`
- provider-specific ingestion logic belongs in `ingestion-service`

### 2. Respect event-driven boundaries

Do not collapse async flows back into tight service coupling unless there is a strong reason.

### 3. Favor practical design over premature platform work

This project already demonstrates backend complexity well. Future work should favor:

- frontend MVP
- end-to-end demoability
- useful docs

over:

- extra infrastructure layers
- new microservices without product need
- deep framework abstractions with little payoff

### 4. Watch the next pressure points

Likely future pressure points:

- `ingestion-service` growing as new providers are added
- duplicated security/error conventions across services
- frontend contracts and browser flow through Gateway

## When Designing Changes

Always ask:

1. Which service should own this behavior?
2. Does it need to be synchronous or event-driven?
3. Does it improve the product, or only the architecture?
4. Does it fit the current V1 scope?
5. Would a future frontend consume this cleanly through Gateway?

## Good Near-Term Decisions

- improve docs and runbook
- keep API contracts stable and browser-friendly
- design frontend around Gateway-only access
- extract patterns only when duplication is becoming costly

## Avoid

- introducing stack assumptions from other ecosystems
- turning V1 into a platform rewrite
- adding new infra tools without a concrete use case
