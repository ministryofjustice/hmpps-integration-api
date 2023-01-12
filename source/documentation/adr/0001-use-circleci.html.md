---
title: 0001 Use CircleCI
weight: 9
---

# 0001 - Use CircleCI as the continuous integration and delivery pipeline

2023-01-06

### Status

Accepted

### Context

We need to select a suitable CI pipeline to build and deploy this service.
Market research was done and both [CircleCI](https://circleci.com/) and [GitHub Actions](https://github.com/features/actions) were considered.
Both these services are feature complete and operate in a similar way.

### Decision

We have decided to use CircleCI as the build pipeline.

### Consequences

1. An additional service will be introduced in addition to GitHub.
2. Much less configuration will be required as pre-defined [HMPPS orbs](https://circleci.com/developer/orbs/orb/ministryofjustice/hmpps) are available in CircleCI.
3. Secrets will be managed in CircleCI.