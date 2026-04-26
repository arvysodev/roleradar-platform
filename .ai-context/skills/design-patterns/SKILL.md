# Design Patterns

Use this skill when deciding whether a pattern is worth introducing in `roleradar-platform`.

## Goal

Choose patterns that solve real code pressure, not patterns for pattern's sake.

## Patterns Most Likely To Matter Here

### Strategy

Best fit when behavior varies by provider, policy, or mode.

Likely use cases:

- multiple ingestion providers
- alternative filtering or ranking policies
- future notification channels

Good signal:

- repeated branching on source/type
- repeated loops with only one or two varying steps

### Factory

Useful when object creation becomes conditional or wiring becomes noisy.

Possible use cases:

- provider-specific ingestion components
- event translator creation
- frontend-side client adapters later if needed

Avoid simple factories that only wrap `new` without reducing complexity.

### Template Method

Useful when a workflow shape is fixed but a few steps differ.

Potential use case:

- ingestion workflows with shared pagination/publish flow but different fetch/mapping steps

Prefer composition or strategy if inheritance would be more rigid than helpful.

### Decorator

Useful when adding cross-cutting behavior around an existing dependency.

Potential use cases:

- logging around outbound clients
- metrics around event publishing
- retry wrappers where framework config is not enough

### Adapter

Useful whenever external provider payloads need translation into internal shapes.

This is already conceptually present in ingestion mapping.

### Observer / Event-Driven Collaboration

Already central to this repository through Kafka.

Treat async domain collaboration as a first-class pattern here:

- auth emits verification event
- notification reacts
- ingestion emits vacancy event
- vacancy reacts

## Anti-Pattern Warnings

Avoid:

- adding factories before creation logic is actually complex
- introducing inheritance when simple composition is clearer
- creating generic "base service" hierarchies across microservices
- abstracting future requirements that do not exist yet

## Pattern Choice Guide

Use:

- Strategy when behavior varies by type/source
- Adapter when translating external models
- Decorator when adding behavior around an existing dependency
- Template Method only if the workflow is truly structurally identical

## Project-Specific Guidance

Right now the most likely legitimate future pattern introduction is Strategy in `ingestion-service`.

The least useful likely move is building a broad shared framework layer for all services too early.
