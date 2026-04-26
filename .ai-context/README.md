# AI Context Pack

This folder contains local context for AI assistants working on `roleradar-platform`.

Goals:

- help future AI sessions understand the project quickly
- keep guidance aligned with the actual stack
- avoid project drift when switching between backend work, docs, and future frontend work

This folder was created specifically for this repository and does not depend on any company-internal project structure.

## Structure

- `agents/` - role-oriented prompt files for common tasks
- `skills/` - focused instructions for recurring workflows

## Current Project Reality

- backend-first microservices monorepo
- Java 21 + Spring Boot
- Gateway + Auth + Ingestion + Vacancy + Notification services
- Kafka-based async ingestion pipeline
- PostgreSQL + Flyway
- Docker Compose local environment
- frontend not implemented yet; frontend MVP is the next major milestone

## What To Prioritize

1. Keep README and docs aligned with implementation.
2. Preserve service boundaries.
3. Prefer small, explainable changes over abstract overengineering.
4. Treat the Gateway as the only browser-facing boundary.
5. When in doubt, optimize for end-to-end product flow, not more backend complexity.
