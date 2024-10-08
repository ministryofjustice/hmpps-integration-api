# HMPPS Integration API Runbook
This is a runbook to document how this service is supported, as described in: [MOJ Runbooks](https://technical-guidance.service.justice.gov.uk/documentation/standards/documenting-how-your-service-is-supported.html#what-you-should-include-in-your-service-39-s-runbook)

Last review date: 15/12/2023

## About the service
This service is composed of a set of long-lived API interfaces to share person related MOJ data with external consumers. 
Prison (Nomis, DPS) and probation (Delius) data is combined from upstream APIs into one cohesive response while masking the source.

### Tech stack 
Containerised Kotlin Spring Boot application running on Cloud Platform’s Kubernetes cluster (eu-west-2). 

AWS API Gateway sits in front of this service with [mutual TLS authentication](https://docs.aws.amazon.com/apigateway/latest/developerguide/rest-api-mutual-tls.html). It does not persist any data and is purely a Facade API.

## Service URLs
- Development: https://dev.integration-api.hmpps.service.justice.gov.uk
- Pre-Production: https://preprod.integration-api.hmpps.service.justice.gov.uk
- Production: https://integration-api.hmpps.service.justice.gov.uk

## Incident response hours
Office hours, usually 9am-6pm on working days.

## Incident contact details
[hmpps-integration-api@digital.justice.gov.uk](mailto:hmpps-integration-api@digital.justice.gov.uk)

## Service team contact
The service team can be reached on MOJ Slack: [#ask-hmpps-integration-api](https://moj.enterprise.slack.com/archives/C04D46K9QTU)

## Other URLs

### Application source code

https://github.com/ministryofjustice/hmpps-integration-api

### Documentation

Source: https://github.com/ministryofjustice/hmpps-integration-api-docs

OpenAPI Specification: https://ministryofjustice.github.io/hmpps-integration-api-docs/documentation/api/index.html

### Cloud platform infrastructure as code

- [Development](https://github.com/ministryofjustice/cloud-platform-environments/tree/main/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-integration-api-dev)
- [Pre-production](https://github.com/ministryofjustice/cloud-platform-environments/tree/main/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-integration-api-preprod)
- [Production](https://github.com/ministryofjustice/cloud-platform-environments/tree/main/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-integration-api-prod)

## Monitoring and alerting dashboards

- [Development](https://grafana.live.cloud-platform.service.justice.gov.uk/d/hmpps-integration-api-dev/hmpps-integration-api-development?orgId=1)
- [Pre-production](https://grafana.live.cloud-platform.service.justice.gov.uk/d/hmpps-integration-api-preprod/hmpps-integration-api-preprod?orgId=1)
- [Production](https://grafana.live.cloud-platform.service.justice.gov.uk/d/hmpps-integration-api-prod/hmpps-integration-api-production?orgId=1)

## Expected speed and frequency of releases

Trunk based development and continuous integration is practiced on this service. If changes pass all automated tests, they are deployed all the way to production.
There is no change request process and the delivery team pushes to production regularly (multiple times a day on average).

## Automatic alerts

There are a number of automatic alerts set up to be delivered into Slack [#hmpps-integration-api-alerts](https://moj.enterprise.slack.com/archives/C052TUCR12L)

These include: 
- Documentation up for review
- Security scan results (Trivy, OWASP, Vera)
- Application exceptions from Sentry
- Failed CircleCI automated tests, image builds, deployments and [system heartbeat](./monitoring-and-alerting/heartbeat.md)

## Impact of an outage

Since we have a variety of consumers, the impact will be different for each of them. In all cases it would prevent civil servants from doing their work and the impact would be quite significant.

## Restrictions on access

Consumers need to be onboarded and go through a mutual TLS authentication. They also need to send a pre-shared key (AWS API Gateway API Key) as a header for identification before being allowed to access the service. 

Once authenticated, there is an authorisation step at the application level to ensure the consumers are allowed to access the requested resources.
There are no IP restrictions in place.

## How to resolve specific issues

### Errors reported to Slack from our monitoring dashboards

There are a number of errors that can be raised on Slack, too many to capture in this document.
  Below are some of the more common errors that could be raised, along with some basic guidance to troubleshoot.

#### API Gateway errors

To see the error in the API Gateway logs, which will contain more details, log into AWS and follow the link to the API Gateway CloudWatch logs which is stored in Kubernetes secrets under `aws-services`.

#### API GW 4xx Error

If it's a 403 Unauthorised error, it indicates that the request is unauthenticated to access the requested resource.

Establish who is trying to access the service, then check:
- Mutual TLS authentication, ensure certificates are valid.
- Check that the API key is correct by checking against the value for the consumer in Kubernetes secrets under `consumer-api-keys`.
- Check that the client certificate contains the correct Common Name (CN), and that it has been added to the Spring Boot application properties, listing allowed paths.

If it's a 404 error, check what path was requested and why the API was unable to serve the request. Could be a miss-typed URL on the client side.

#### API GW 5xx Errors

Establish what sort of 5xx error has occurred by checking the logs. 
For general debugging of 500 errors, please see [troubleshoot 5xx errors for API Gateway](https://repost.aws/knowledge-center/api-gateway-5xx-error)

#### API GW Client Error, API GW ExecutionError, API GW integrationError

When any of these errors occur, and no obvious recent changes have been made that could cause this, it is recommended to re-deploy the API Gateway and application.
Get in touch with Cloud Platform as they will have to approve any pull requests to run the infrastructure pipeline.

It could also be an issue on the AWS side. The AWS [service status page](https://health.aws.amazon.com/health/status) should be checked as well.

#### API GW integrationLatency and latency

This error may not indicate downtime of the service, but should be closely monitored as it will become disruptive.
If no recent changes have been made then re-deploy the application.
It could be an issue on the AWS side. The AWS [service status page](https://health.aws.amazon.com/health/status) should be checked as well.

#### Latency

This error may not indicate downtime of the service, but should be closely monitored as it will become disruptive.
If no recent obvious changes have been made that could cause this, then re-deploy the application.
This API integrates with a number of upstream APIs and the latency could be coming from there.

#### Blocked request

If a large number of blocked requests are observed, it may indicate that someone is trying to gain unauthorised entry.  
The service is only consumed in the UK and if any IPs are known to be from different countries, it should be investigated.

#### CPU > 80%

When the CPU is under strain, it may indicate a number of issues. It may be that there is a high volume of legitimate requests being served, or it can indicate a bug in the service.
First trigger a deployment through the build pipeline to rebuild the application. If this doesn't solve the problem, consider [adding more pods](https://github.com/ministryofjustice/hmpps-integration-api/blob/main/helm_deploy/hmpps-integration-api/values.yaml#L5) to ease the load while the problem can be looked at.

#### memory > 80%

When the memory is under strain, it may indicate a number of issues. It may be that there is a high volume of legitimate requests being served, or it can indicate a bug / memory leak in the service.
First trigger a deployment through the build pipeline to rebuild the application. If this doesn't solve the problem, consider [adding more pods](https://github.com/ministryofjustice/hmpps-integration-api/blob/main/helm_deploy/hmpps-integration-api/values.yaml#L5) to ease the load while the problem can be looked at.

#### less than 2 pods running

At any given time (for any environment), there should be at least 2 pods running for high availability. 
The service also has a zero downtime deployment strategy in place, which means that old pods are gracefully terminated before traffic is redirected to new pods.
Check whether any recent obvious updates have been made that could have caused this regression. Roll back or forward with a fix if necessary and trigger another deployment through the build pipeline.
