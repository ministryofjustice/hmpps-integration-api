# Authentication and Authorisation

This service restricts access by authenticating and authorising each request.
Consumers need to be explicitly added before they can access any of the endpoints.

## Authentication

The primary authentication mechanism is [mutual TLS provided by API Gateway](https://docs.aws.amazon.com/apigateway/latest/developerguide/rest-api-mutual-tls.html).

To identify consumers there is also an API key that needs to be sent by the consumer as a header `x-api-key`, which belongs to a [Usage Plan](https://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-api-usage-plans.html).

For setup instructions on adding new consumers please see [Setting up a new consumer](./guides/setting-up-a-new-consumer.md).
Once authentication has succeeded, authorisation will take place.

## Authorisation

The service implements path based authorisation in a fail closed approach.
Each path that needs to be accessed by the consumer will have to be explicitly allowed in the service configuration (under the authorisation section) eg.
https://github.com/ministryofjustice/hmpps-integration-api/blob/main/src/main/resources/application-dev.yml

This needs to be configured per environment, there is a configuration file for each.

The name of the consumer has to match the Common Name (CN) in the Subject Distinguished Name (SDN) of the certificate that was created in the [Setting up a new consumer](./guides/setting-up-a-new-consumer.md) step.
If the Common name doesn't match the name in the configuration file, the consumer will receive 403 Forbidden responses.

To see the details of the certificate run:

```bash
openssl x509 -in ./consumer.pem -text
```

This will output the Subject Distinguished Name, in the form of :
```
Subject: C = GB, ST = London, L = London, O = Home Office, CN = consumer
```

Note that the CN (Common Name) is the value that will be checked on each request as this is passed as a header by API Gateway.
The name of this header is `subject-distinguished-name`.

A Spring Boot Filter was introduced to check each request for this header, and allow or disallow the current path being requested.

The implementation of this logic can be found [here](https://github.com/ministryofjustice/hmpps-integration-api/blob/main/src/main/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/extensions/AuthorisationFilter.kt). 

![authorisation flow](./diagrams/integration_api_auth.drawio.png)

## Access Control

Access control for the External API is based on three concepts...

* Permissions
* Filters
* Redactions

If you think of the data provided by the API as a set of tables, each with rows and columns, then...

* Permissions specify which tables a consumer has access to
* Filters specify which rows the consumer has access to
* Redactions specify which columns the consumer can see

> Note that permissions are often referred to in the code as "includes" for historical reasons

Permissions are "deny-by-default"; a consumer cannot access an endpoint unless they are explicitly given permission.

Filters and redactions are generally "allow-by-default"; consumers can see everything provided by the endpoint unless 
explitly prevented. Note, however, that there are some mandatory filters such as for Limited Access Offenders.

## Roles

Rather than defining the permissions, filters and redactions for individual consumers, we typically define them for
a "Role", and then assign roles to consumers.

Consumers can have different roles in the development, pre-production and production environment, but the roles 
themselves are part of the application code and therefore the same in all environments.

## Consumer Event Queue Access

Consumers can be notified of changes to the data they have access to via the HMPPS External Events service.

This provides an AWS SQS queue for each consumer, with notification messages for changes to any API endpoints that the 
consumer has permissions for.

A `/token` API endpoint is provided that allows the consumer to request a short-lived AWS STS access token that provides 
access to their SQS queue, and the filtering of notifications to only include those for endpoints they have permissions
for is handled by a custom SNS subscription for each consumer.

The implementation of the token endpoint, consumer queues, subscriptions and access control is all defined in the Cloud 
Platform Environments repo:

https://github.com/ministryofjustice/cloud-platform-environments/tree/main/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-integration-api-dev/resources

* `api_gateway-assume-role-endpoint.tf` creates the `/token` endpoint and the temporary assumed role for accessing the SQS 
queue. Note that the assumed role is given a `ClientId` tag with the consumer common name as the value. 

* `event-subscriber-{consumer}.tf` creates the SQS queue and SNS subscription for a particular consumer.

* `resource "aws_iam_role_policy" "sqs"` in `iam.tf` sets up the permissions for the assumed roles to access the SQS 
queues using a filter on the `ClientId` tag on the assumed role.
