# Java Code Review

Use this skill when reviewing Java changes in this repository.

## Review Priorities

Focus on issues that are likely to matter in production or maintenance:

- incorrect behavior
- risky exception handling
- broken transactional assumptions
- weak event-processing guarantees
- security mistakes
- unnecessary complexity

## Java Review Checklist

### Null and data handling

Check for:

- assumptions about non-null data from external systems
- chained calls without safe reasoning
- leaking `null` where empty values or explicit handling would be clearer

### Exceptions

Check for:

- swallowing exceptions
- overly broad `catch (Exception)`
- loss of useful context when rethrowing
- scheduler code that hides failures too aggressively

### Transactions

In Spring services, check:

- whether multi-step mutations are inside the right transaction boundary
- whether recovery paths need their own transaction
- whether async processing relies on behavior that a transaction might delay or roll back

### Collections and streams

Prefer readability over cleverness.

Flag:

- streams that obscure control flow
- repeated conversions or unnecessary intermediate lists
- logic that is clearer as an ordinary loop

### Concurrency and async correctness

Relevant especially for Kafka and idempotent flows.

Check for:

- race windows around "exists then save" logic
- assumptions about exactly-once behavior where only at-least-once is realistic
- state changes that are not safe under retry or duplicate delivery

### Security and boundaries

Check for:

- sensitive values in logs
- weak JWT or cookie handling changes
- endpoints accidentally exposed or made too permissive
- trust in user input without validation

### Performance and data access

Check for:

- obvious N+1 patterns
- repeated work inside loops
- expensive behavior hidden in mapper or repository usage
- growing ingestion logic that duplicates provider loops

## Output Style

Prefer:

- concrete findings
- file references
- brief explanation of impact
- suggested fix direction

If no serious issues are found, state that clearly and mention residual risks or missing test coverage.
