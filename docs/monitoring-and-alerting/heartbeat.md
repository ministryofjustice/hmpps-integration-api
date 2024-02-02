# Heartbeat
A service developed by the HMPPS Integration API team. The service is intended to be a standalone container capable of 
making requests which mimic consumers using HMPPS Integration API.

Heartbeat performs a configurable simple request against the API (see [Setup](#setup)). Heartbeat performs the request
and makes an assertion on the response. If the response contains the assertion, a success is reported to CircleCI.

> ⚠️ The current implementation of Heartbeat expects the response to contain a `firstName` property; 
> Without this, Heartbeat will result in a fail.

It is recommended that corresponding logs within [Sentry](sentry.md) are checked in the instance of a failed Heartbeat.
These are likely to reveal what went wrong server side.

### Intervals
Heartbeat will run on a scheduled interval. This is an ongoing health check to ensure that the HMPPS Integration API has 
no downtime. In the event of a failure, team members are notified.

### Deployment
During the deployment pipeline, Heartbeat runs before the next environment can be deployed. For instance, `pre-prod` 
cannot be deployed if Heartbeat had reported failure on `dev`. This provides a safety net to prevent critical errors from 
propagating to our production environment.

## Setup
Heartbeat is already configured in each of the environments. But the steps below can be followed to set up Heartbeat in a
new environment.

1. Ensure `/scripts/heartbeat.sh` exists in the `hmpps-integration-api` repository.
2. Create a new API key (if one does not already exist in the cloud platform namespace). This [PR](https://github.com/ministryofjustice/cloud-platform-environments/pull/13647)
can be used as a template.
3. Generate client certificates for Heartbeat using the `/scripts/generate-client-certificate.sh` script.
4. Create a new context in CircleCI. The name should be `hmpps-integration-api-heartbeat-<environment>`
5. Add the following environment variables:
   - `API_KEY` - The consumer API key
   - `MTLS_CERT` - Base64 encoded client .PEM file
   - `MTLS_KEY` - Base64 encoded client .KEY file
   - `SERVICE_URL` - The HMPPS Integration API URL to request
6. Configure a new job within `/.circleci/config.yml` to run Heartbeat when desired. Existing examples of the interval
and deployment jobs can be found in the config. Ensure the context passed into the job matches the context created 
in CircleCI.

## Steps
Job runs can be viewed from the CircleCI dashboard.


