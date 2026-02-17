# 0018 - Use JdbcTemplate for database access by default

## Status

Decision status: Proposed

## Context

We are migrating the `hmpps-integration-events` code into the `hmpps-integration-api` code base so that we can retire
the integration-events service. The core of integration-events is a database table for notifications, which means that
the migrated integation-api code will need to access that database.

The integration-events code

## Options

### A) Continue with JPA

TBC

### B) Switch to JdbcTemplate by default [recommended]

TBC

### C) Switch to JdbcTemplate exclusively

Use JdbcTemplate for all future database connectivity with use of JPA prohibited.

## Decision

TBC

## Consequences

TBC
