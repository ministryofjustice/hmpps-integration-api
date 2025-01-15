# 0012 - Endpoint Naming Strategy

2024-09-06

## Status

Accepted

## Context

The HMPPS Integration API is intended as a general method of providing data interfaces to HMPPS digital services. The HTTP endpoints are partitioned using the internal HMPPS domain model and provide data on these areas of the criminal justice domain. In most cases access to a subset of the general endpoints should be sufficient to provide a client with a suitable data interface to HMPPS systems and this is expected to be the normal operating model. In some cases it may be necessary to partition the data in different ways for a specific client. This may be for security, performance or other technical reasons. The API infrastructure and development process supports providing these extra endpoints on a client-specific basis. We would like to ensure the integration design and documentation is consistent and clear with rules for naming both general and specific endpoints. This should aid ongoing development of the API and help both API management teams and clients understand which endpoints are generic and which are intended as client-specific.

## Decision

Name HTTP URLs according to the following rules:

1. Follow the [ADR documented](./0007-version-through-url-path.md) strategy for versioning in the URL path
2. For client-specific endpoints use the URL path part immediately after the version part to indicate the specific client
3. Use subsequent URL path parts to either:
   1. Indicate the HMPPS domain that the endpoint relates
   2. **OR** indicate the intention of a client-specific endpoints

### Rationale

- The URL path should indicate the overall intention of the HTTP endpoint
- The URL path should indicate if the endpoint is intended for a specific client immediately after the version path part, supporting grouping of domain endpoints and client-specific endpoints in the documentation

## Consequences

- Developers of future endpoints will have a clear guidance on naming HTTP URLs to reflect the intention of the endpoint
- General domain-based endpoints will be named in a clear and consistent manner
- Client-specific endpoints will be named in a clear and consistent manner, indicating the client the endpoint has been designed for

## Examples

| Type            | Description                                                                           | URL Path                                                |
| --------------- | ------------------------------------------------------------------------------------- | ------------------------------------------------------- |
| HMPPS Domain    | An endpoint that provides a persons cell location                                     | `/v1/persons/{encodedHmppsId}/cell-location`            |
| HMPPS Domain    | An endpoint that provides risk assessment actuarial scores                            | `/v1/persons/{encodedHmppsId}/risks/scores`             |
| Client-Specific | An endpoint to provide consolidated person details to support the EPF digital service | `/v1/epf/person-details/{encodedHmppsId}/{eventNumber}` |
