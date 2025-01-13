# 0013 - Filtering data from an endpoint

2025-01-13

## Status

Accepted

## Context

Similarly to ADR 0012, we have identified use-cases where a category of consumer needs access to a subset of the same category of data. The motivating example is private prisons, where different prison operators require access to the same data, but only for the people in their prisons. In this case we are expecting multiple operators of private prisons to need different subsets of data from the same endpoints.

## Decision

Add the concept of filters to the consumer configuration in the `application-*.yml` files.

```yml
example:
  include: # Authorised endpoints move to "include" field
    - "/v1/prison/prisoners/[^/]*$"
    - "/v1/prison/prisoners"
  filters: # Filters determine what subset of data is returned
    prisons: # This "example" consumer is only allowed data for the prisons with IDs 'ABC', 'DEF' and 'XYZ'
      - ABC
      - DEF
      - XYZ
```

In the above case, when calling the `/v1/prison/prisoners` API endpoint the search would be prefiltered to only include prisoners from prisons with IDs 'ABC', 'DEF' and 'XYZ', and when calling the `/v1/prison/prisoners/{hmppsId}` endpoint, if the prisoner is not in one of the prisons with IDs 'ABC', 'DEF' and 'XYZ', they would not be returned the data.

To all intents and purposes, if the data is filtered out it should be treated as if it does not exist, for instance this means we return 404 error response codes instead of 403 error response codes.

For the sake of backwards compatibility, and to allow consumers to access all the data at an endpoint, if a filter is missing, then all data should be returned:

```yml
all-access:
  include:
    - "/v1/prison/prisoners/[^/]*$"
    - "/v1/prison/prisoners"
  filters: # No prisons filter so access to ALL prisoners
no-access:
  include:
    - "/v1/prison/prisoners/[^/]*$"
    - "/v1/prison/prisoners"
  filters:
    prisons: # Empty prisons filter so access to NO prisoners
```

These filters should be implemented at the service layer. They might be implemented as:

- Rejecting the request using information in the call, for instance where the prison ID is in the url for the endpoint
- Rejecting the request using information from the response, for instance when the response includes the prison ID
- Making an additional request to get information, for instance making an additional call to get prisoner

### Rationale

- Unlike in ADR 0012, we are expecting multiple private prisons to require access to multiple endpoints. This would make it unmanageable to maintain client specific endpoints.
- By controlling the filters at this layer, this allows us to continue managing authorisation at application level, following ADR-0009.
- Returning 404 responses instead of 403 responses, prevents consumers of the API for scanning for prisoners at other prisons.

## Consequences

- When configuring a new consumer, the filters must be configured alongside the endpoints.
- Endpoints must implement the filters that are appropriate.
- Endpoints must document which filters they enforce (so that new consumers are only given access to endpoints that are properly restricted).
