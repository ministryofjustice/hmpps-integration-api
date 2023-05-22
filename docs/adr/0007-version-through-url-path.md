# 0007 - Version through URL path

2023-05-22

## Status

Accepted

## Context

Our [change and versioning strategy](https://ministryofjustice.github.io/hmpps-integration-api-docs/api-changes-and-versioning.html#api-changes-and-versioning) outlines broadly how change will be managed to ensure stability and support for consumers.

In the event that a breaking change to the API must be made, we will increment the API version and manage the migration with consumers.

We considered several approaches to API versioning:

**1. URL path**

Example:
```
https://integration-api.hmpps.service.justice.gov.uk/v1/persons?first_name=Frank
```
* This strategy involves putting the version number in the path of the URL. Typically, API designers use the version to refer to their application version rather than the endpoint version.

Considerations:
* Highly transparent, because of visibility in the URL path.
* Versioning at the level of the whole application would create a need to carry over any non-changed endpoints from version to version.
* Versioning at the level of endpoints may result in a situation where endpoints are not in-sync, and so a consumer might need to use v1 of the ```/persons``` endpoint alongside v2 of the ```/images``` endpoint. This may be considered confusing.

**2. Query parameters**

Example:
```
https://integration-api.hmpps.service.justice.gov.uk/persons?version=1&first_name=Frank
````
* This strategy involves adding a query parameter to the request that indicates the endpoint version. 

Considerations:
* Flexible and specific, as the version exists on the endpoint level.
* As above, may result in a situation where endpoints are not in-sync, and so one might be using different versions for different endpoints concurrently. 

**3. Headers**

Example:
```
Accept: version=1.0
```
* This strategy involves adding a header to the request that denotes the version.

Considerations:
* Flexible and specific, as the version exists on the endpoint level.
* Not very transparent, as the version is buried in the request object.
* Requires custom headers.
* Doesn't clutter the URI.

## Decision

We have decided to use URL path versioning. 

This approach was preferred because:
- The version is highly visible in the URL path.
- Query parameters are used elsewhere in the API, such as for the search ```/persons``` endpoint. It may be confusing for consumers for query parameters to be used for unlike elements, such as for person attributes and also endpoint version (```/persons?version=1&first_name=Frank```).

We also decided to version at the endpoint level, rather than at the level of the entire API. 

This approach was preferred because:
- It gives us a more granular control over versioning, creating the ability to limit the scope of breaking changes to one or a few endpoints rather than the entire system.
- It creates a smaller footprint in the code base, removing the need to carry over any non-changed endpoints from version to version.
- It results in an improved repository folder structure whereby the controllers for different endpoints are grouped together by version, with changes easily identifiable.

## Consequences

- Versioning will be applied at the level of each individual endpoint.
- Consumers need to explicitly specify the version of an endpoint they are requesting. If the version is missed, the consumer will get a 404 NOT FOUND response.
- We must encourage all consumers to migrate to the latest version of endpoints as soon as possible as legacy endpoints will only remain in the system for a period of time.
- We will need to decide upon a method of notifying consumers when a new endpoint version is available.
- We will need to continue support for old endpoints to allow for backwards compatibility until all consumers are migrated to the new version.