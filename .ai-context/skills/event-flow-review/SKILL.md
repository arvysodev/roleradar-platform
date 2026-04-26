# Event Flow Review

Use this skill when changing Kafka-related code or reviewing asynchronous flows.

## Current Event Flows

### Vacancy ingestion

- `ingestion-service` publishes vacancy upsert events
- `vacancy-service` consumes them
- `vacancy-service` records processed event IDs
- vacancy persistence is updated or refreshed

### Email verification

- `auth-service` publishes email verification requested events
- `notification-service` consumes them
- notification side effects happen asynchronously

## What Must Stay True

- duplicate delivery should not create duplicate business results
- ownership stays with the consumer service that persists the canonical data
- event payloads remain explicit and understandable
- failure handling should be visible and not silently corrupt state

## Review Checklist

Check for:

- missing idempotency checks
- event schema drift without consumer alignment
- swallowing exceptions in a way that hides broken flows
- bypassing race recovery on vacancy upserts
- retry/DLT handling becoming inconsistent with existing behavior

## When Refactoring

Be careful not to:

- couple ingestion directly to vacancy persistence
- turn async boundaries into cross-service synchronous assumptions
- make event semantics depend on provider-specific quirks
