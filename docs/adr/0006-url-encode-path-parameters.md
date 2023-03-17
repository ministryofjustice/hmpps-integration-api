# 0006 - URL-encode path parameters that contain forward slashes

2023-03-16

## Status

Accepted

## Context

Users must be able to search the system for nominals on Police National Computer (PNC) ID. PNC ID is a unique person
identifier. RESTful API design convention is that path parameters are used to identify a specific resource or resources.
However, passing PNC IDs into our API as path parameters is problematised by the fact that PNC IDs contain a forward
slash (e.g., 12/345345B) which is the URO standard path delimiter.

We considered two options for dealing with the forward slash in PNC ID:    
**1. Encode the PNC ID**

- The PNC ID 12/345345B would be passed to the /persons end-point URL-encoded as 12%2F345345B.

Considerations:

- This would result in some added complexity for the consumer, who would have to encode the PNC ID.

**2. Use query parameters to pass the PNC ID to the API**

- The request format would become /persons?pnc_id={pncId} rather than /persons/{pncId}.

Considerations:

- Query parameters should be used to sort or filter on resources and so this usage is not best practice.
- It is best practice to use logical nesting on end-points to show relationships. Using query parameters for PNC ID is
  likely to cause confusion by detaching it from that with which it is most closely associated. An example is
  persons/images?pnc_id=id, where PNC ID relates foremost to persons rather than images but the URL distorts this
  relationship.

## Decision

We have decided that, in the case of path parameters that contain forward slashes, we will URL-encode the parameter
rather than use query parameters.

This choice was preferred because:

- It aligns with RESTful API convention.
- It manages the complexity at the level of the parameter (here, PNC ID), rather than allowing the use of query
  parameters to affect the design of the API (and so the usability) more broadly by confusing the relationships between
  entities.

## Consequences

- The documentation will need to explain the URL encoding process clearly for consumers. 
