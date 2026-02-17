# 0018 - Use JdbcTemplate for database access by default

## Status

Decision status: Proposed

## Context

We are migrating the `hmpps-integration-events` code into the `hmpps-integration-api` code base so that we can retire
the integration-events service. The core of integration-events is a database table for notifications, which means that
the migrated integation-api code will need to access that database.

The integration-events code use Spring Data JPA for accessing the database, but this is quite a heavyweight framework 
that includes a lot of component scanning and code generation. The overhead of this framework is often justified by
the benefits of the complex object-relation-mapping (ORM) that JPA supports, but the External API database does not 
contain complex domain entities, only simple technical state tracking.

The Spring Framework offers a simpler alternative for direct SQL access to database based on the `JdbcTemplate` class.
This handles all of the technical plumbing, but requires the user to specify SQL commands rather than defining the 
query using Kotlin concepts.

## Options

### A) Continue with JPA

We could simply migrate the existing JPA-based code from integration-events to integration-api as-is.

### B) Switch to JdbcTemplate by default [recommended]

Keep the existing repository interface, but remove the JPA annotations and provide a custom implementation of that
interface that is built using `JdbcTemplate`.

Use `JdbcTemplate` by default for all future database access in the HMPPS External API, but allow for JPA to be used 
where appropriate.

### C) Switch to JdbcTemplate exclusively

Use JdbcTemplate for all future database connectivity, with use of JPA prohibited.

## Decision

TBC

## Consequences

* The External API code will be simpler and more explicit, with reduced build-time overheads from component scanning.
* The External API database access code may be less familiar to other HMPPS developers who are used to using JPA. 
