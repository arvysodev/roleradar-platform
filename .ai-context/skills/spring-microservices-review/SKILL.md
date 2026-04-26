# Spring Microservices Review

Use this skill when reviewing backend code in this repository.

## Review Focus

### Service boundaries

Check whether behavior lives in the right service:

- Gateway for browser boundary concerns
- Auth for identity/token logic
- Ingestion for provider integration
- Vacancy for vacancy ownership
- Notification for outbound email side effects

### Clean code

Look for:

- unclear naming
- overly large services
- mixed concerns in one class
- duplicated validation or mapping logic
- avoidable branching that signals a missing abstraction

### Spring-specific checks

Look for:

- missing transactional boundaries on multi-step mutations
- hidden framework behavior that makes code harder to reason about
- overly broad exception handling
- config drift between `application.yml`, env files, and runtime assumptions

### Event-driven correctness

Look for:

- idempotency regressions
- changes that break event schema expectations
- accidental bypass of processed-event logic
- race handling getting removed or weakened

### Testing quality

Look for:

- whether tests verify behavior, not just wiring
- where Testcontainers dependence is appropriate
- missing high-value edge cases
- brittle tests that encode implementation details

## Good Review Output

Prefer concise findings with:

- severity
- affected file
- why it matters
- suggested fix direction
