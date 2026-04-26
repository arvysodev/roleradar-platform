# Architecture Notes

This document is meant as a quick re-entry point after time away from the project.

## System Goal

RoleRadar aggregates vacancies from external providers, normalizes them, stores them in a dedicated vacancy service, and exposes them through a gateway-protected API.

The current architecture is backend-first and event-driven.

## Main Flow

### 1. Vacancy ingestion flow

1. `ingestion-service` fetches jobs from external APIs.
2. Provider payloads are mapped into a common `VacancyUpsertedEvent`.
3. The event is published to Kafka.
4. `vacancy-service` consumes the event.
5. `vacancy-service` checks whether the event was already processed.
6. If not processed, it upserts the vacancy by `(source, externalId)`.
7. The processed event is recorded to support idempotency.

Important implementation ideas:

- source-specific mapping happens in `ingestion-service`
- data ownership belongs to `vacancy-service`
- duplicate event delivery is expected and handled
- insert races are handled via database constraints plus recovery logic

Relevant code:

- `services/ingestion-service/src/main/java/.../service/IngestionService.java`
- `services/vacancy-service/src/main/java/.../service/VacancyEventProcessingService.java`
- `services/vacancy-service/src/main/java/.../service/VacancyIngestionService.java`
- `services/vacancy-service/src/main/java/.../service/VacancyRaceRecoveryService.java`

### 2. Auth flow

1. Client calls the Gateway.
2. Gateway forwards auth requests to `auth-service`.
3. `auth-service` handles register/login/refresh/logout.
4. Access token and refresh token are issued by `auth-service`.
5. Gateway stores tokens in cookies for browser use.
6. For protected requests, Gateway validates the access token and forwards the bearer token downstream.

Important implementation ideas:

- browser-facing auth boundary is the Gateway
- downstream services trust validated JWTs
- `auth-service` is responsible for issuing tokens and exposing JWKS

Relevant code:

- `services/gateway/src/main/java/.../config/SecurityConfig.java`
- `services/gateway/src/main/java/.../security/ForwardBearerTokenGatewayFilter.java`
- `services/auth-service/src/main/java/.../service/AuthService.java`

### 3. Email verification flow

1. User registers through `auth-service`.
2. `auth-service` creates a verification token and publishes an email verification event.
3. `notification-service` consumes the event.
4. `notification-service` sends the email.
5. User opens verification link routed through Gateway to `auth-service`.

Relevant code:

- `services/auth-service/src/main/java/.../event`
- `services/notification-service/src/main/java/.../service/EmailNotificationService.java`

## Service Boundaries

### Gateway

Owns:

- browser boundary
- cookie handling
- JWT validation for incoming browser requests
- routing to backend services

Does not own:

- user data
- vacancy data
- ingestion logic

### Auth Service

Owns:

- users
- refresh tokens
- email verification token state
- JWT issuing
- JWKS

### Vacancy Service

Owns:

- vacancy persistence
- processed event persistence
- vacancy lifecycle jobs
- vacancy query API

### Ingestion Service

Owns:

- external source integration
- normalization to shared vacancy event shape
- scheduling of ingestion jobs

### Notification Service

Owns:

- email delivery side effects
- consumption of email verification events

## Data Stores and Messaging

- `auth-service` -> Postgres `auth_db`
- `vacancy-service` -> Postgres `vacancy_db`
- Kafka -> async communication between ingestion/auth and downstream consumers
- Mailpit -> local mail sink for development

## Why Microservices Here?

This project could be modeled as a modular monolith.

It is intentionally split into services to practice:

- async boundaries
- JWT and gateway concerns
- isolated persistence per bounded area
- event-driven processing
- deployment-oriented thinking

## Current Architectural Strengths

- clear service ownership
- realistic async pipeline
- idempotency awareness
- race-condition handling in vacancy ingestion
- CI and operational concerns already present

## Current Architectural Pressure Points

These are not urgent problems, but they are likely next pressure points:

- `ingestion-service` will grow as more providers are added
- duplicated security/problem-details patterns may be worth extracting later
- frontend is missing, so the product flow is not yet demonstrated end-to-end

## Suggested Next Milestone

Build a frontend MVP that talks only to the Gateway and proves the end-to-end user journey:

- login/register
- browse vacancies
- filter/search vacancies
- open vacancy details
