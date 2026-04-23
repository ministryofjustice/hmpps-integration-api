# 0019 - On-behalf-of authorisation in HMPPS External API

## Status

Decision status: ✅ Accepted

## Context

“On-behalf-of” (OBO) authorisation refers to the processes where a system or service is explicitly acting on behalf
of another person. This is in contrast to situations where the system/service presents only its own service credentials
(so user-level access control is not possible), or directly impersonates the end-user
(so the name of the system/service cannot be included in audit logs).

As-of April 2026 the HMPPS External API does not support OBO authorisation, but there are some scenarios where this
might be useful.

The challenge of implementing on-behalf-of user-level access control can be divided into two parts…

1. Propagating the end user’s identity, along with the service credentials, in a trusted manner
2. Applying data access controls based on the propagated end-user identity

These two challenges can be addressed almost entirely independently.

## Options

### Propagating the end user’s identity.

#### 1a) Implement a new OBO “proxy” service

A separate OBO proxy would ensure that this complexity does not impact the External API, given that most External
API usage would not require it.

However, a key downside would be that we would need to choose (for each external client) between…

- Supporting OBO authorisation
- Supporting all other External API features such, such as LAO redaction or offender ID conversion
- Duplicating those other features in the OBO proxy

#### 1b) Adding OBO authorisation capability to the existing HMPPS External API

If OBO authorisation were to be added to the External API then it would need to be an optional feature. With this
approach, clients would not be required to provide an OBO end-user identity, but if they do then that identity can be
used by the API to provide additional data elements or reduce the need for redaction.

### Applying data access controls based on the propagated identity.

#### 2a) Add user-level access control to the External API / OBO Proxy

This option has the advantage that the solution is centralised and other services + teams are not impacted. However,
he user-level access control functionality would not be reusable for internal service, and this approach would require
a single team to have a full understanding of the detailed data access control requirements for the entirety of prisons
and probations data. The access control could be implemented on the “downstream” data structures, but this would make
the implementation highly-specific to the External API. Alternatively the implementation could be based on the
“upstream” data structures, though this would result in exactly the same code as for option 2 without the benefit
of reusability.

#### 2b) Add user-level access control to the upstream APIs

This option distributes the problem across all HMPPS service teams, which has both advantages and disadvantages. The
implementation would be reusable, and the approach would make best use of domain data knowledge within the various
service teams.

#### 2c) Create a new user-level access control service

This option has most of the drawbacks and benefits of option 2a, but increases the likelihood of internal reuse of
the access control functionality in exchange for increased technical complexity.

## Decision

On 21st April 2026 the HMPPS Digital Principal Technical Architects selected options 1b and 2a.

## Consequences

The HMPPS External API will be extended to support optional on-behalf-of authorisation, and will propagate the
end user identity to upstream APIs. End-user access control will be implemented in those upstream APIs.
