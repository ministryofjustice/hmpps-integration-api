# 0014 - Visit Notifications

Decision date: 2025-03-04

## Status

Decision status: Accepted

## Context

Private prisons are now required to provide a copy of data they collect to the MoJ, 
and this includes data on visits.

HMPPS have a general preference for all visits to be booked through DPS, though with
a recognition that this may not always be practical.

A number of options were considered for implemented the integration points required to
support this...

1. DPS Visits Led - Nexus is a front end (get available slots from DPS and choose one of those)
2. Nexus Led - send to DPS (DPS service accepts whatever visit Nexus provides)
3. Nexus Led - send to NOMIS (Same but send visits into NOMIS)
4. Nexus Led - send to other database (Same but send visits into a new data store)

## Decision

We will proceed with option 2 in the short term, and allow for implementation of option 1
in future.

A new API will be created that will receive a Visit notification, perform some basic
synchronous validation, and then queue the notification for asynchronous processing by DPS. 

### Rationale

Options 3 and 4 were quickly rejected: #3 directly includes NOMIS integration which would need 
to be replaced anyway, and #4 involves the creation of an entirely new, and unnecessary, component. 

Option 1 is the overall MoJ architecture preference, but it is accepted that this is likely more
development effort for all parties, and it is unlikely that we will be able to enforce it for
all prisons anyway. 

It will be challenging to implement option 1 in a manner that complies with our asynchronous
writes policy. This would be a new integration pattern for HMPPS.

It is anticipated that in the long run both #1 and #2 will be supported, with option #1 being
the HMPPS preferred approach.

## Consequences

* DPS will have to accept whatever visit information is sent, as long as it passes the initial
  synchronous validation performed by the Integration API.
* The prison provider system must not book the visit if the initial synchronous validation fails.
* Any problems encountered when processing the notification after the initial synchronous 
  validation and response will be the responsibility of HMPPS and not the prison provider.
* Neither the private prison system nor HMPPS will be able guarantee capacity and lack of  
  conflicts due visits being booked in multiple systems.
* Does not stop private prisons implementing visits that go against national policy. 
  
## Supporting Documentation

* [Discussion Document, Google Docs](https://docs.google.com/document/d/1NA9OzNn6GEBQZCc1Ba4dllOZLwXK_NTM2uZdC607oJg/edit?tab=t.0#heading=h.exkvg6tmp7ez)
* [Integration API schema for Visit](https://ministryofjustice.github.io/hmpps-integration-api/documentation/api/index.html#schema-dataresponsevisit)
* [Visits implementation epic](https://dsdmoj.atlassian.net/browse/HMAI-2)
