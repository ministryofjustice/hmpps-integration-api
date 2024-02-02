# 0003 - Manually manage OpenAPI specification file

2023-01-30

## Status

Accepted

## Context

The [OpenAPI specification](https://spec.openapis.org/oas/latest.html) is a standard for describing REST APIs and
[Swagger UI](https://swagger.io/tools/swagger-ui/) is used to visualise and interact with a specification file in more
user-friendly manner. These are used widely used within HMPPS to document APIs,
e.g. [Prison API's OpenAPI specification](https://api.prison.service.justice.gov.uk/v3/api-docs)
and [Prison API's Swagger UI](https://api.prison.service.justice.gov.uk/swagger-ui/index.html). This is achieved by
using the [SpringDoc OpenAPI library](https://springdoc.org) and annotating the codebase which the library then uses to
expose two endpoints: one for the OpenAPI specification in JSON format and another for Swagger UI.

## Decision

In [ministryofjustice/hmpps-integration-api#19](https://github.com/ministryofjustice/hmpps-integration-api/pull/19), we
followed precedence and added OpenAPI annotations to our codebase. However,
in [ministryofjustice/hmpps-integration-api-docs#11](https://github.com/ministryofjustice/hmpps-integration-api-docs/pull/11)
we decided to change our approach to manually managing a OpenAPI specification file and using
the [feature of the tech docs template to convert the file into documentation](https://tdt-documentation.london.cloudapps.digital/write_docs/add_openapi_spec/#convert-an-openapi-specification-into-documentation). This approach presents us with
a number of benefits over the annotation method:

1. Codebase is not polluted with lots of annotations.
2. Code for endpoints do not have to exist in order for them to be documented. This means that we can define endpoints that will exist and provide documentation as early as possible to future consumers like a contract and provides them with the option to create a simulator based on the file. Furthermore, we can use it as part of feature development of API endpoints, akin to a design prototype - Documentation Driven Development.
3. GOV.UK/Ministry of Justice style - with annotations we are stuck with the Swagger UI style.
4. Content is all in one place and we can be flexible with it i.e. add sections around auth, architecture, ADRs, release notes, rate limiting etc. more easily.

## Consequences

1. We will manually manage our OpenAPI specification file instead of using annotations in our codebase to generate one.
2. Our OpenAPI specification will be used to automatically generate documentation.
3. We will remove OpenAPI annotations in our codebase.
