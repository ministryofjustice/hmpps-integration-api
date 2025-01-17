# 0011 - Revert decision 0003 and auto-generate OpenAPI specification

2024-08-16

## Status

Proposed

## Context

In [#0003](./0003-manually-manage-openapi-file.md), the team decided to manually manage the OpenAPI specification file, as opposed to generating them from code.
This had the benefit of allowing the team to write documentation for endpoints before they have been built.
However, in practice this has resulted in an out-of-date specification due to code changes not being reflected.

## Decision

Revert decision 0003 and auto-generate OpenAPI specification using the [Springdoc OpenAPI library](https://springdoc.org).
When the code or annotations change, the OpenAPI specification will be generated and published automatically.
Additional documentation can be added for endpoints that have not yet been implemented, by adding a separate "draft" OpenAPI specification file.

### Rationale

- Auto-generating OpenAPI specifications from annotations in code is consistent with the wider HMPPS approach
- Keeping documentation as close to the relevant code as possible ensures it is kept up-to-date
- Pre-existing documentation is maintained
- Additional documentation that doesn't correspond to code can be drafted separately

## Consequences

- The existing OpenAPI yaml file will be converted into code annotations, and will be published automatically via GitHub pages.
- The tech-docs pages will be updated to reference the new location.
