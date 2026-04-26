# SOLID Principles

Use this skill when reviewing class design, refactoring services, or deciding whether a new abstraction is justified.

## Goal

Apply SOLID practically, not dogmatically.

This project is a Spring Boot microservices monorepo. The right question is not "Can we invent another interface?" but "Does this change make the code easier to evolve without harming clarity?"

## What To Look For In This Repository

### S - Single Responsibility

Watch for services that start mixing:

- transport concerns
- persistence concerns
- external provider logic
- event publication/consumption logic
- formatting/presentation logic

Examples of healthy separation in this repo:

- Gateway handles browser/security boundary concerns
- Auth handles identity/token concerns
- Ingestion handles provider fetch + normalization
- Vacancy handles canonical vacancy persistence and lifecycle

Signs of SRP drift:

- one service class grows around multiple unrelated workflows
- a class knows too much about different providers or different domains
- a controller or consumer starts containing business logic that belongs in a service

### O - Open/Closed

This matters most when behavior grows by variants.

In this project, the common pressure point is `ingestion-service`.

If adding a new vacancy source means:

- touching one giant class
- adding more `if/else`
- copying page-loop logic again

then a strategy-style split may be justified.

Do not introduce extensibility too early. Add it when the next source or behavior would otherwise make the code worse.

### L - Liskov Substitution

Most useful here when introducing interfaces or alternate implementations.

Check that:

- alternate implementations preserve behavior contracts
- mocks/fakes used in tests behave like real implementations where it matters
- abstractions do not hide incompatible semantics

### I - Interface Segregation

Prefer focused interfaces over broad "manager" contracts.

Good use cases:

- repository-like abstractions
- outbound client abstractions
- email sending or event publishing boundaries

Avoid:

- giant interfaces that mix reads, writes, orchestration, and lifecycle operations

### D - Dependency Inversion

Depend on abstractions when it improves testability or decoupling.

Good candidates in this project:

- outbound integrations
- provider-specific ingestion implementations
- side-effect boundaries such as notifications

Do not add interfaces around every Spring bean just because you can.

## Healthy Heuristics For This Project

Prefer:

- small service classes with one clear role
- explicit transaction boundaries
- clean separation between sync HTTP flow and async event flow
- code that reads clearly without needing a diagram for every method

Be skeptical of:

- over-abstracted V1 code
- factories/strategies introduced before a second real variant exists
- interfaces with only one implementation and no real pressure for indirection

## Review Questions

Ask:

1. Does this class have more than one real reason to change?
2. Will the next feature require modifying a central class repeatedly?
3. Is this abstraction helping tests or boundaries, or just adding ceremony?
4. Is the dependency direction aligned with service ownership?
