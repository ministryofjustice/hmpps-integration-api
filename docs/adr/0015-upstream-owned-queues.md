# 0015 - Upstream Owned Queues

Decision date: 2025-05-01 (Integration API Steering Committee)

## Status

Decision status: Accepted

## Context

A previous decision says that, apart from in exceptional cases, writes from external consumers through the integration API should all be asynchronous. As we are aiming to minimise the size of the integrations with upstream services, we've had cases where the upstream API only has a single upstream write endpoint. This has led to the suggestion that we operate queues in the Integration API layer, with a worker that calls the standard upstream APIs.

## Decision

We have decided that the queue providing the asynchronous integration and any DLQ must be owned by the upstream API. The work to create this queue and create the worker that reads off the queue could be done by another team.

### Rationale

If we follow the pattern of the Integration API owning the queue, we would reach a situation where a single team was responsible for managing events over several different domains. We believe that a single team would not be able to maintain the knowledge required to do this.

This would particularly be an issue when managing undelivered messages, as an Integration API team would only know how to resolve technical issues, and other business and data related issues would require response from the upstream service team.

## Consequences

- Teams that operate upstream APIs will be required to support queues in order to receive writes from external consumers.
- The Integration API team will need to apply any validation rules defined by the upstream teams before writing to the queue.

## Supporting Documentation

- [Integration patterns](https://dsdmoj.atlassian.net/wiki/spaces/HIA/pages/5281742897/Integration+Patterns#External-Clients-Triggering-Internal-HMPPS-Processes)
