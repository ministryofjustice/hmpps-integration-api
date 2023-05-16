# Sentry

Exceptions thrown by the API are picked by Sentry. Sentry keeps records of when an exception was thrown, how
many times it's occurred as well as details about the request; a stack trace is also available to aid in
debugging the issue.

An integration with the Slack Justice Digital workspace ensures that exceptions are not missed by posting them to the
[hmpps-integration-api-alerts](https://mojdt.slack.com/archives/C052TUCR12L) channel.

## Setup

Access to Sentry is provided to all members of the Ministry of Justice GitHub organisation as outlined in
their [documentation](https://operations-engineering.service.justice.gov.uk/documentation/services/sentry.html#sentry-io).

1. Once you have access, log in to the [Projects Dashboard](https://ministryofjustice.sentry.io/projects/)
2. Click `Join a Team`
3. Search for `hmpps-integration-api`
4. Join the team

## Steps

1. Select an environment from the [Projects Dashboard](https://ministryofjustice.sentry.io/projects/)
