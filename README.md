# HMPPS Integration API <!-- omit in toc -->

[![CircleCI](https://circleci.com/gh/ministryofjustice/hmpps-integration-api/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/hmpps-integration-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://ministryofjustice.github.io/hmpps-integration-api)
[![repo standards badge](https://img.shields.io/badge/dynamic/json?color=blue&style=flat&logo=github&label=MOJ%20Compliant&query=%24.result&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fhmpps-integration-api)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-github-repositories.html#hmpps-integration-api "Link to report")

## Contents <!-- omit in toc -->

- [About this project](#about-this-project)
  - [External dependencies](#external-dependencies)
  - [Data Analysis Tooling](#data-analysis-tooling)
- [Get started](#get-started)
  - [Using IntelliJ IDEA](#using-intellij-idea)
- [Usage](#usage)
  - [Running the application](#running-the-application-locally)
    - [With dependent services](#with-dependent-services)
  - [Running the tests](#running-the-tests)
  - [Running the linter](#running-the-linter)
  - [Running all checks](#running-all-checks)
  - [Request logging](#request-logging)
- [Further documentation](#further-documentation)
- [Developer guides](#developer-guides)
- [Related repositories](#related-repositories)
- [License](#license)

## About this project

A long-lived API that exposes data from HMPPS systems such as the National Offender Management Information System (NOMIS), nDelius (probation system) and Offender Assessment System (OASys), providing a single point of entry for
consumers. It's built using [Spring Boot](https://spring.io/projects/spring-boot/) and [Kotlin](https://kotlinlang.org/)
as well as the following technologies for its infrastructure:

- [AWS](https://aws.amazon.com/) - Services utilise AWS features through Cloud Platform such
  as [Elastic Container Registry (ECR)](https://aws.amazon.com/ecr/) to store our built artifacts as well as [Simple Storage Service (S3)](https://aws.amazon.com/s3/). The CI/CD pipeline
  stores and retrieves them from there as required.
- [CircleCI](https://circleci.com/developer) - Used for our build platform, responsible for executing workflows to
  build, validate, test and deploy our project.
- [Cloud Platform](https://user-guide.cloud-platform.service.justice.gov.uk/#cloud-platform-user-guide) - Ministry of
  Justice's (MOJ) cloud hosting platform built on top of AWS which offers numerous tools such as logging, monitoring and
  alerting for our services.
- [Docker](https://www.docker.com/) - The API is built into docker images which are deployed to our containers.
- [Kubernetes](https://kubernetes.io/docs/home/) - Creates 'pods' to host our environment. Manages auto-scaling, load
  balancing and networking to our application.

![Context Diagram](docs/diagrams/context.svg)

### External dependencies

This solution is dependent on:

- [Prison API](https://github.com/ministryofjustice/prison-api)
- [Prisoner Search](https://github.com/ministryofjustice/hmpps-prisoner-search)
- [Probation Offender Search](https://github.com/ministryofjustice/probation-offender-search)
- [External API and nDelius](https://github.com/ministryofjustice/hmpps-probation-integration-services/tree/main/projects/external-api-and-delius)
- [Effective proposal framework and Delius](https://github.com/ministryofjustice/hmpps-probation-integration-services/tree/main/projects/effective-proposal-framework-and-delius)
- [HMPPS Auth](https://github.com/ministryofjustice/hmpps-auth)
- [Assess Risks and Needs (ARNS)](https://github.com/ministryofjustice/hmpps-assess-risks-and-needs-coordinator-api)
- [Adjudications](https://github.com/ministryofjustice/hmpps-manage-adjudications-api)
- [Case Notes](https://github.com/ministryofjustice/offender-case-notes)
- [Create and Vary License](https://github.com/ministryofjustice/create-and-vary-a-licence)
- [Incentives](https://github.com/ministryofjustice/hmpps-incentives-api)
- [Manage POM Case](https://github.com/ministryofjustice/hmpps-manage-pom-cases-api)
- [Non-associations](https://github.com/ministryofjustice/hmpps-personal-relationships-api)
- [Personal Relationships](https://github.com/ministryofjustice/hmpps-personal-relationships-api)
- [Education and Work Plan](https://github.com/ministryofjustice/hmpps-education-and-work-plan-api)
- [Prisoner Alerts](https://github.com/ministryofjustice/hmpps-alerts-api)
- [Manage Prison Visits](https://github.com/ministryofjustice/hmpps-manage-prison-visits-orchestration)
- [Risk Management](https://github.com/ministryofjustice/hmpps-assess-risks-and-needs)
- [Locations Inside Prison](https://github.com/ministryofjustice/hmpps-locations-inside-prison-api)
- [Activities Management](https://github.com/ministryofjustice/hmpps-activities-management-api)
- [Health and Medication](https://github.com/ministryofjustice/hmpps-health-and-medication-api)
- [Support for additional needs](https://github.com/ministryofjustice/hmpps-support-additional-needs-api)

These things depend upon this solution:

- Consumer Applications (MAPPS)

### Data Analysis Tooling

Included within this repository is Python-based functionality used by the team to enhance data researching and analysis.

> You can find information on how to use this in the [data_analysis](scripts/data_analysis/README.md) section of our scripts.

## Get started

### Using IntelliJ IDEA

When using an IDE like [IntelliJ IDEA](https://www.jetbrains.com/idea/), getting started is very simple as it will
handle installing the required Java SDK and [Gradle](https://gradle.org/) versions. The following are the steps for
using IntelliJ but other IDEs will prove similar.

1. Clone the repo.

   ```bash
   git clone git@github.com:ministryofjustice/hmpps-integration-api.git
   ```

2. Launch IntelliJ and open the `hmpps-integration-api` project by navigating to the location of the repository.

   Upon opening the project, IntelliJ will begin downloading and installing necessary dependencies which may take a few
   minutes.

3. Enable pre-commit hooks for formatting, linting, and secret scanning.

   ```
    # Install pipx if not already installed
    brew install pipx

    # Ensure the path to pipx-installed tools is active
    pipx ensurepath
    # Restart your terminal after running this

    # Install pre-commit
    pipx install pre-commit

    # Install hooks into .git/hooks
    pre-commit install
   ```

## Usage

### Running the application locally

To run the application using IntelliJ:

1. Start dependencies using `make serve-dependencies`
2. Select the `HmppsIntegrationApi` run configuration file.
3. Click the run button.

Or, to run the application using the command line:

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

Then visit [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html).

#### With dependent services

Simulators are used to run dependent services for running the application locally and in smoke tests.
They use [Prism](https://github.com/stoplightio/prism) which creates a mock server
based on an API's latest OpenAPI specification file.

It's possible to run the application with dependent services like
the [NOMIS / Prison API](https://github.com/ministryofjustice/prison-api)
and [HMPPS Auth](https://github.com/ministryofjustice/hmpps-auth) with Docker
using [docker-compose](https://docs.docker.com/compose/).

1.  Build and start the containers for each service.

    ```bash
    make serve
    ```

    Each service is then accessible at:
    - [http://localhost:8080](http://localhost:8080) for this application
    - [http://localhost:4010](http://localhost:4010) to [http://localhost:40XX]() for mocked HMPPS APIs
    - [http://localhost:9090](http://localhost:9090) for the HMPPS Auth service

2.  To call the integration-api, you need to pass a distinguished name in the `subject-distinguished-name` header. The `CN` attribute should match the client you wish to access the service as.
    The list of clients and their authorised endpoints can be found in [application-local-docker.yml](src/main/resources/application-local-docker.yml).

        For example,

        ```bash
        curl -H "subject-distinguished-name: O=local,CN=all-access" http://localhost:8080/health
        ```

        As part of getting the HMPPS Auth service running
        locally, [the in-memory database is seeded with data including a number of clients](https://github.com/ministryofjustice/hmpps-auth/blob/main/src/main/resources/db/dev/data/auth/V900_0__clients.sql). A client can have different permissions i.e. read, write, reporting, although strangely the column name is called `​​autoapprove`.

3.  If you wish to call an endpoint of a dependent API directly, an access token must be provided that is generated from the HMPPS Auth
    service. Use the following cURL to generate a token for a HMPPS Auth client.

    ```bash
    curl -X POST "http://localhost:9090/auth/oauth/token?grant_type=client_credentials" \
      -H 'Content-Type: application/json' \
      -H "Authorization: Basic $(echo -n "hmpps-integration-api-client:clientsecret" | base64)"
    ```

    This uses the client ID: `hmpps-integration-api-client` and the client secret: `clientsecret`. A number of seeded
    clients use the same client secret.

    A JWT token is returned as a result, it will look like this:

    ```json
    {
      "access_token": "eyJhbGciOiJSUzI1NiIs...BAtWD653XpCzn8A",
      "token_type": "bearer",
      "expires_in": 3599,
      "scope": "read write",
      "sub": "hmpps-integration-api-client",
      "auth_source": "none",
      "jti": "Ptr-MIdUBDGDOl8_qqeIuNV9Wpc",
      "iss": "http://localhost:9090/auth/issuer"
    }
    ```

    Using the value of `access_token`, you can call a dependent API using it as a Bearer Token.

    There are a couple of options for doing so such as [curl](https://curl.se/),
    [Postman](https://www.postman.com/) and using in-built Swagger UI via the browser e.g.
    for Prison API at [http://localhost:4030/swagger-ui/index.html](http://localhost:4030/swagger-ui/index.html) which documents the
    available API endpoints.

### Running the tests

The testing framework used in this project is [Kotest](https://kotest.io/).

To run the tests using IntelliJ:

1. Install the [Kotest IntelliJ plugin](https://kotest.io/docs/intellij/intellij-plugin.html). This provides the ability to easily run a test as it provides run buttons (gutter icons) next to each test and test
   file.
2. Click the run button beside a test or test file.

To run unit and integration tests using the command line:

```bash
make test
```

To run unit tests using the command line:

```bash
make unit-test
```

To run integration tests using the command line:

```bash
make integration-test
```

### Running the linter

To lint the code using [Ktlint](https://pinterest.github.io/ktlint/):

```bash
make lint
```

To autofix any styling issues with the code:

```bash
make format
```

### Running all checks

To run all the tests and linting:

```bash
make check
```

### Request logging

This can be done within `logback-spring.xml`. To enable request logging, update the value of the `level` property within
the logger named `<application>.config.RequestLogger` to the desired
[logger level](https://docs.spring.io/spring-boot/docs/2.1.13.RELEASE/reference/html/boot-features-logging.html).

```xml
<logger name="uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.RequestLogger" additivity="false" level="DEBUG">
    <appender-ref ref="logAppender"/>
    <appender-ref ref="consoleAppender"/>
</logger>
```

Note, this will only specifically enable the `RequestLogger`.

## Further documentation

- [Architecture Decision Records (ADRs)](/docs/adr)
- [Architecture diagrams](/docs/diagrams)
- [Audit](/docs/audit.md)
- [Authentication and Authorisation](/docs/authentication_and_authorisation.md)
- [Backups](/docs/backups.md)
- [Banner](/docs/banner.md)
- [Environments](/docs/environments.md)
- [High availability](/docs/high-availability.md)
- [Monitoring and Alerting](/docs/monitoring-and-alerting/README.md)
- [Security](/docs/security.md)
- [Performance](/docs/performance.md)
- [Runbook](/docs/runbook.md)

## Developer guides

- [Certificate Renewal](/docs/guides/certificate-renewal.md)
- [Creating an environment](/docs/guides/creating-an-environment.md)
- [Renew mutual TLS server certificate](/docs/guides/renew-mutual-tls-server-certificate.md)
- [Setting up a new consumer](/docs/guides/setting-up-a-new-consumer.md)
- [Setting up a CircleCI context for deployment](/docs/guides/setting-up-circleci-context-for-deployment.md)
- [Updating diagrams](/docs/guides/updating-diagrams.md)
- [Useful commands](/docs/guides/useful-commands.md)
- [Validating Upstream Responses](/docs/guides/validating-upstream-responses.md)

## Related repositories

| Name                                                                                                   | Purpose                                                                |
| ------------------------------------------------------------------------------------------------------ | ---------------------------------------------------------------------- |
| [HMPPS Integration Events](https://github.com/ministryofjustice/hmpps-integration-events)              | Creates integration events triggered by upstream MoJ domain events.    |
| [HMPPS Integration API Documentation](https://github.com/ministryofjustice/hmpps-integration-api-docs) | Previously provided documentation for API consumers. No longer in use. |

## License

[MIT License](LICENSE)
