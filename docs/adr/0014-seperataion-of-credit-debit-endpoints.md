# 0014 - Separate endpoints for Debit and Credit transactions

2025-01-31

## Status

Rejected

## Context

In the process of accurately recording prisoner transactions there is a requirement to be able to record both payments in (credits) and payments out (debits). We propose that both a credit and debit endpoint are provided so that there is a clear statement of intent that could help reduce user error.

## Decision

We should only provide one endpoint for recording transactions that accepts both credit and debit records rather than two separate endpoints.

### Rationale

- NOMIS determines whether a transaction is a credit or debit from the category of transaction.
- The discovery for the new prisoner finance system is not at a point where it is known whether this would change

## Consequences

- There will be no additional logic within the API
- There may be a need for another round of finance integration API development by the finance team but that could have also been the same if two endpoints had been implemented
