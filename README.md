# RoleRadar

RoleRadar is a backend-focused pet project designed as a microservices-based job aggregation platform.

> ## Architectural note  
> This project is intentionally implemented using a microservices + event-driven architecture, even though the domain could be handled with a simpler modular monolith.  
> The goal is to demonstrate practical experience with:
> - Kafka and asynchronous processing
> - idempotency and race condition handling
> - service boundaries and data ownership
> - API Gateway as a security boundary (JWT + cookies)
> - production-oriented backend design

---

## Architecture

The system consists of the following services:

- **API Gateway**
    - Handles browser cookies
    - Validates access tokens
    - Forwards Bearer tokens downstream
    - Acts as the single entry point (browser boundary)

- **Auth Service**
    - Handles authentication
    - Issues and signs JWT tokens
    - Manages refresh tokens (store/revoke)
    - Exposes JWKS
    - Provides `/me` endpoint

- **Ingestion Service**
    - Fetches external job APIs (Arbeitnow, Remotive, Adzuna)
    - Normalizes data
    - Publishes Kafka events

- **Vacancy Service**
    - Owns vacancy data (source of truth)
    - Deduplication via `(source, externalId)`
    - Handles race conditions
    - Supports filtering, sorting, searching

- **Notification Service**
    - Sends emails (e.g. verification)
    - Handles event-driven notifications

---

## Key Features

- Event-driven pipeline: ingestion → Kafka → vacancy service
- Idempotent processing (DB constraints + processed events)
- Race condition handling (e.g. unique constraint conflicts)
- JWT auth with HttpOnly cookies via Gateway
- Refresh token flow with revocation
- External API ingestion with configurable limits
- Scheduled ingestion jobs
- Email verification via Kafka → notification service
- Global exception handling
- MapStruct-based mapping
- Health checks per service

---

## Tech Stack

- Java 21, Spring Boot
- Spring Security (JWT, Resource Server)
- Spring Cloud Gateway (WebFlux)
- PostgreSQL + Flyway
- Kafka
- Docker

---

## Current Status (V1)

Implemented:
- Core services (gateway, auth, ingestion, vacancy, notifications)
- Kafka-based ingestion pipeline
- JWT + refresh token flow
- Idempotency and basic retry handling
- Scheduled ingestion from 3 sources

TODO:
- Frontend
- Redis
- Tests
- CI/CD
- Additional sources (e.g. Greenhouse)

---

## Future Improvements (V2)

- Profile service (user preferences, tags, skills)
- Key rotation + multiple JWKS keys
- Stronger event processing guarantees
- Improved retry/backoff strategies
- Startup resilience between services
- Analytics service
- AI-based job recommendations
- Kubernetes deployment

---

## Notes

- Adzuna API keys are not included due to provider policy
- Some retry/concurrency mechanisms are intentionally simplified and planned for V2  
