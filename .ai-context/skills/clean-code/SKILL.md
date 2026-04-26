# Clean Code

Use this skill when improving readability, consistency, and maintainability without changing architecture unnecessarily.

## Goal

Make the code easier to understand a month from now.

That matters a lot in this repository because it is a personal project that may be paused and resumed between interview prep and other work.

## What Clean Code Means Here

### Clear naming

Prefer names that reveal role, not implementation trivia.

Good names explain:

- whether something is a consumer, publisher, service, mapper, or config
- whether a method validates, persists, transforms, publishes, or routes

### Small understandable units

Prefer methods and classes that can be understood quickly.

Watch for:

- methods doing validation, persistence, logging, and side effects all together
- service classes that become the default place for unrelated logic

### Explicit control flow

Avoid cleverness that slows down comprehension.

Prefer:

- straightforward branching
- early returns when they reduce nesting
- extracted helper methods when they clarify intent

### Consistency across services

This repo benefits from consistent conventions in:

- exception handling
- config naming
- DTO naming
- event naming
- service layering

### Comments

Comments should explain why, not restate what.

Good reasons to comment:

- non-obvious trade-off
- race-condition handling
- idempotency rationale
- why a transaction boundary is unusual

Bad reasons to comment:

- repeating obvious code behavior
- narrating every line

## Project-Specific Smells

Watch for:

- provider-specific ingestion logic leaking into generic layers
- controller/consumer classes taking over service responsibilities
- config values spread in confusing ways across env and application config
- duplicated logic that drifts across services

## Refactoring Bias

Prefer low-risk clarity improvements over broad rewrites.

Good changes:

- rename ambiguous methods
- extract focused helpers
- split oversized classes when responsibilities are already distinct
- align naming across services

Be cautious with:

- generic utility layers
- premature shared modules
- massive refactors during active feature work
