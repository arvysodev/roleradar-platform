# Runbook

This is a practical "how do I get back into the project quickly?" guide.

## 1. What is already here?

Backend services implemented:

- `gateway`
- `auth-service`
- `ingestion-service`
- `vacancy-service`
- `notification-service`

Infrastructure in `compose.yml`:

- Postgres for auth
- Postgres for vacancy
- Kafka
- Kafka UI
- Mailpit

Frontend:

- `frontend/roleradar-web` exists only as a placeholder for now

## 2. Environment files

Example env files already exist:

- `.env.auth.example`
- `.env.gateway.example`
- `.env.ingestion.example`
- `.env.notification.example`
- `.env.vacancy.example`

Local runtime files expected by Compose:

- `.env.auth`
- `.env.gateway`
- `.env.ingestion`
- `.env.notification`
- `.env.vacancy`

Auth service also expects a JWT private key mounted into the container:

- `services/auth-service/secrets/auth/jwt-private.pem`

## 3. Local startup

From repository root:

```powershell
docker compose -f compose.yml up --build
```

Useful local endpoints:

- Gateway: `http://localhost:8080`
- Swagger UI via Gateway: `http://localhost:8080/swagger-ui.html`
- Kafka UI: `http://localhost:8085`
- Mailpit: `http://localhost:8025`

## 4. Service ports

- Gateway: `8080`
- Auth: `8081`
- Vacancy: `8082`
- Ingestion: `8083`
- Notification: `8084`
- Postgres auth: `5433`
- Postgres vacancy: `5434`
- Kafka: `9092`

## 5. Main manual flows to verify

### Auth flow

- register user
- inspect Mailpit for verification email
- open verification link
- log in through Gateway
- confirm cookies are set
- call protected endpoint through Gateway

### Ingestion flow

- run ingestion endpoint or wait for schedule
- inspect Kafka UI if needed
- verify vacancies appear in `vacancy-service`
- verify default vacancy listing via Gateway

### Vacancy lifecycle flow

- confirm vacancies can be queried
- confirm filters/search/sorting behave correctly
- confirm stale close and cleanup jobs still make sense for the current data model

## 6. Tests

Service tests can be run per service from each service directory:

```powershell
.\gradlew.bat test --no-daemon
```

Notes:

- `gateway`, `ingestion-service`, and `notification-service` are easier to verify in constrained environments
- `auth-service` and `vacancy-service` integration tests rely on Testcontainers
- if Docker daemon is not available, those tests may fail before application logic is exercised

## 7. Current code areas worth remembering

If you only have 10 minutes, revisit these areas first:

- `services/auth-service/src/main/java/.../service/AuthService.java`
- `services/gateway/src/main/java/.../config/SecurityConfig.java`
- `services/gateway/src/main/java/.../security/ForwardBearerTokenGatewayFilter.java`
- `services/ingestion-service/src/main/java/.../service/IngestionService.java`
- `services/vacancy-service/src/main/java/.../service/VacancyEventProcessingService.java`
- `services/vacancy-service/src/main/java/.../service/VacancyIngestionService.java`
- `services/vacancy-service/src/main/java/.../service/VacancyRaceRecoveryService.java`

## 8. Known non-issues that can look scary

- local Gradle caches can create lots of unversioned files if `GRADLE_USER_HOME` is pointed into the repo
- integration tests may fail because Docker/Testcontainers is unavailable, not because business logic is broken
- the README may drift unless updated alongside major project changes
