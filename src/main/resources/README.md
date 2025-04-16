# `application-*.yml` files

The `application-*.yml` files (e.g. `application-dev.yml`) contain the per environment configs. These include the deployed environments, (`dev`, `preprod`, `prod`), as well as testing environments (e.g. `integration-test`). These files set environment specific configuration. 

## `services`

Lists the services that are used by the Integration API. Includes for each service:
- the endpoint that will be used
- any additional configuration for the service

## `feature-flag`

Lists any feature flags that are currently in place in the project and whether the flag is enabled in the environment. 

## `authorisation.consumers` 

Access to the API is controlled on a consumer-by-consumer and endpoint-by-endpoint basis. The mechanism for this is specified in [our documentation](../../../docs/authentication_and_authorisation.md).

This access is specified in the `roles`, `include` and `filters`. 

### `roles`

The primary way that a consumer should gain access to endpoints. The `globals.yml` file defines roles that can be added to a consumer and the endpoints they allow access to. 

### `include`

Any additional endpoints that are allowed but do not fit into a suitable role.

### `filters`

Restricts access to data from endpoints. This is to allow endpoints to be used by a wider audience, by preventing them from having access to data that they shouldn't have access to. 
