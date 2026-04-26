# Next Steps

This document is intentionally opinionated. It is meant to answer: "What should I do next without re-deciding the whole roadmap?"

## Current Recommendation

The next major milestone should be the frontend.

Reason:

- backend foundations are already good enough to support a real UI
- the project already demonstrates strong backend engineering
- the biggest missing piece for product feel and portfolio impact is end-to-end user experience

## Recommended Frontend MVP

Build a thin client that talks only to the Gateway.

Suggested MVP scope:

1. auth screens
2. vacancy list page
3. vacancy filters and search
4. vacancy detail page
5. loading, empty, and error states

Nice to have later:

- saved filters
- profile/preferences
- favorites/bookmarks
- dashboards/analytics

## What Not To Do First

Avoid starting with:

- Kubernetes
- Redis
- AI recommendations
- new microservices
- broad platform refactors

Those can all come later. Right now they add complexity faster than product value.

## Backend Follow-Ups Worth Deferring Until Needed

These are good ideas, but not the highest-leverage next step:

- extracting shared security/problem-details patterns
- splitting `ingestion-service` into provider strategies as sources grow
- stronger retry/backoff and startup resilience
- additional observability/metrics

## Suggested Short-Term Sequence

### Step 1

Finish the docs and keep them current.

Definition of done:

- root README is accurate
- `docs/architecture.md` stays aligned with implementation
- this file stays focused on the next real milestone

### Step 2

Decide frontend stack and folder structure.

Good initial target:

- keep frontend isolated under `frontend/roleradar-web`
- consume only Gateway endpoints
- build a clean MVP, not a giant product shell

### Step 3

Implement the minimum end-to-end path:

- register
- verify email
- log in
- view vacancies
- open vacancy details

### Step 4

After the frontend MVP exists, revisit backend polish:

- API ergonomics
- docs/demo flow
- observability
- source expansion

## Portfolio Angle

If you want the project to read better in a portfolio, the fastest improvement is not "more backend complexity".

The fastest improvement is:

- visible frontend
- clearer demo flow
- cleaner README screenshots/diagrams
- a documented end-to-end user journey
