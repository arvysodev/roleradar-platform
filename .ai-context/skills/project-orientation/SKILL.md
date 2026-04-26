# Project Orientation

Use this skill at the start of a session when the assistant needs fast context on the repository.

## What This Project Is

RoleRadar is a backend-first job aggregation platform implemented as a microservices monorepo.

The backend is the main completed part of the project today. The frontend is the next major milestone.

## Core Services

- `gateway` - browser/security boundary
- `auth-service` - auth, JWT, refresh tokens, JWKS, email verification state
- `ingestion-service` - fetches external job sources, maps payloads, publishes Kafka events
- `vacancy-service` - vacancy source of truth and query API
- `notification-service` - email delivery

## Primary Runtime Flow

1. User calls Gateway.
2. Gateway routes browser-facing auth and vacancy traffic.
3. Ingestion fetches external jobs and publishes vacancy events.
4. Vacancy consumes events and persists canonical vacancy data.
5. Auth publishes verification events.
6. Notification consumes them and sends email.

## What To Read First

1. `README.md`
2. `docs/architecture.md`
3. `docs/runbook.md`
4. `docs/next-steps.md`

## Key Code Areas

- `services/auth-service/src/main/java/.../service/AuthService.java`
- `services/gateway/src/main/java/.../config/SecurityConfig.java`
- `services/gateway/src/main/java/.../security/ForwardBearerTokenGatewayFilter.java`
- `services/ingestion-service/src/main/java/.../service/IngestionService.java`
- `services/vacancy-service/src/main/java/.../service/VacancyEventProcessingService.java`
- `services/vacancy-service/src/main/java/.../service/VacancyIngestionService.java`
- `services/vacancy-service/src/main/java/.../service/VacancyRaceRecoveryService.java`

## Session Rules

- prefer working with the current documented architecture, not imagined future architecture
- assume frontend is not yet implemented unless files prove otherwise
- prefer changes that improve product readiness or clarity
- if proposing bigger backend changes, explain why they matter before implementation
