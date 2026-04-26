# Frontend MVP Planning

Use this skill when planning or implementing the first real frontend for RoleRadar.

## Goal

Build the smallest frontend that makes the project feel like a product.

## Current Constraint

The frontend should talk only to the Gateway.

Do not plan browser traffic directly to downstream services unless the architecture is intentionally changed.

## MVP Scope

Prioritize:

1. registration
2. login
3. email verification follow-through
4. vacancy list
5. vacancy filters/search
6. vacancy detail page
7. loading, empty, and error states

## Non-Goals For First Pass

- analytics dashboards
- user preferences service
- AI recommendations
- multi-step personalization
- major design-system abstraction

## Backend Contract Expectations

Frontend work should validate:

- cookie-based auth flow through Gateway
- protected vacancy API access through Gateway
- browser-friendly error handling
- predictable pagination/filter semantics

## Planning Questions

Before implementation, define:

1. Which pages exist?
2. Which API routes are needed through Gateway?
3. What is the auth lifecycle from user perspective?
4. What data shape does the vacancy list need?
5. What states must the UI handle gracefully?

## Success Criteria

The MVP is successful if someone can:

1. create an account
2. verify email
3. log in
4. browse vacancies
5. open a vacancy detail view

That is enough to turn the backend into an end-to-end portfolio demo.
