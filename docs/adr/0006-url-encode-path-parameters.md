# 0006 - URL-encode path parameters that contain forward slashes

2023-03-16

## Status

Accepted

## Context

Users must be able to search the system for a person by Police National Computer (PNC) ID. PNC ID is a unique person
identifier. RESTful API design convention would typically state that path parameters are used to identify a specific
resource or resources.
However, passing PNC IDs into our API as path parameters is problematised by the fact that PNC IDs contain a forward
slash (e.g., 12/345345B). Forward slashes are path delimiters, so any requests containing a forward slash would not
correctly be routed to our controller.

We considered two options for dealing with the forward slash in PNC ID:    
**1. Encode the PNC ID**

- The PNC ID 12/345345B would be passed to the `/persons` end-point URL-encoded as 12%2F345345B.

Considerations:

- This would result in some added complexity for the consumer, who would have to encode the PNC ID.

**2. Use query parameters to pass the PNC ID to the API**

- The request format would become /persons?pnc_id={pncId} rather than /persons/{pncId}.

Considerations:

- RESTful API convention is that path parameters should be used to identify a specific resource or resources, so
  the usage of query parameters here would be atypical.
- It is best practice to use logical nesting on end-points to show relationships. Using query parameters for PNC ID is
  likely to cause confusion by detaching it from that with which it is most closely associated. An example is
  our `/persons/{pncId}/images` end-point that retrieves images related to the person with the specified PNC ID. If
  the decision were taken to use query parameters for PNC ID, the path would become `persons/images?pnc_id=pncId`; PNC
  ID relates foremost to persons rather than images but this relationship is distorted by the URL.

## Decision

We have decided that, in the case of path parameters that contain forward slashes, we will URL-encode the parameter
rather than use query parameters.

This choice was preferred because:

- It aligns with RESTful API convention.
- It manages the complexity at the level of the parameter (here, PNC ID), rather than allowing the use of query
  parameters to affect the design of the API (and so the usability) more broadly by confusing the relationships between
  entities.

## Consequences

- Consumers will need to URL-encode path parameters that contain a forward slash.
- The documentation will need to explain the URL encoding process clearly for consumers.
- This API uses Apache Tomcat, a web server and Servlet container for Java code. There is a reported security
  vulnerability in the case that Tomcat is used behind a proxy, whereby using URL-encoded path parameters exposes a
  directory traversal vulnerability. This vulnerability may allow attackers to work around the context restrictions of
  the proxy. Consequently, this API should not at any point in the future be used as a proxy.
