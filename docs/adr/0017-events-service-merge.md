# 0017 - Merge Integration Events into Integration API

## Status

Decision status: Accepted

## Context
The HMPPS External API (formerly “integration” API) is the passive participant in external integrations, listening for requests from external consumers and responding to them.

Alongside the API is the HMPPS External Events service, which is our active participant in these integrations, sending notifications to those external consumers when data exposed by the API may have changed.

There are three main areas of functionality in the External Events service…

* Subscribing to domain events and writing them into a database table for deduplication

* Sending SNS messages based on the notifications in the database table

* Managing the SNS subscriptions for consumer queues

The reasons for implementing the notifications as a separate service from the API were not recorded at the time the decision was made, but it is likely that it was done to minimise the impact on the API service at a time when there was significant pressure to deliver functionality rapidly.

No specific advantages have been identified for maintaining a separate service for notifications, and several drawbacks have been identified…

* Any repository- or pipeline-level administration overhead is doubled

* The migration from CircleCI to GitHub Actions is an example of this

* When a change impacts both pipelines the two parts must be coordinated manually

* The events codebase and pipeline are slightly less mature than the api versions

  * the API has separate unit and integration tests, but events has them merged
  * the API has a fully automated CD pipeline, events requires manual deployment approvals

* There is code that is common to both services; we currently just duplicate this code, though it would be possible to extract it into a common library

* There is configuration that is required by both services; the api provides an endpoint specifically for the events service to read this API configuration

* As we add more filters for API endpoint results it will be increasingly cumbersome to implement corresponding subscription filters, or increasingly problematic for consumers receiving update notifications for data that will be filtered out from their responses

## Options
### A) Big-bang code merge
It should be possible to simply copy all the code from hmpps-external-events into hmpps-external-api, make any necessary changes or fixes, and stop the events service.

While this would avoid the need to handle intermediate states with the events functionality spread across two services, those intermediate states are already well understood. There are likely significant challenges that will arise during the implementation of a single change on this scale, however.

### B) Merge the services gradually [recommended]
We have identified a viable step-by-step approach to migrating the hmpps-external-events code and functionality into hmpps-external-api, utilising the fact that the three core areas of events service functionality are largely independent.

* Migrate the consumer SNS subscription management into the API codebase

* Give the API access to the event notifications database, exposing new API endpoints to provide enhanced access to the database for testing and support

* Migrate the Domain Event subscription functionality into the API codebase

* Migrate the external notification publishing functionality into the API codebase

* Shut down and retire the external events service, including archiving the code repository

There are currently multiple External Events service instances that coordinate event and notification processing using “claim IDs” in the database, and having additional instances of the code running in a different service should not be an issue. We would use feature toggles in both services to avoid this though.

The subscription management is not time critical as it does nothing unless a consumer role changes, which is quite rare, so we would just stop the events version before starting the API version.

### C) Single code repo, multiple services
Another option would be to keep the services deployed separately but from a single code repository. This would make it easier to share code and configuration between the services.

### D) Continue with independent services
We can continue to work with independent external API and Events services, accepting some of the drawbacks and working over time to resolve or mitigate some of the others.

## Decision
The HMPPS External API Steering Committee agreed to proceed with option B on 21 Jan 2026.

## Consequences
* Merging the services will take a insignificant amount of effort, and the time spent doing this will not be available for other work.

* Once implemented, a modest improvement to developer productivity would be expected. There is unfortunately no realistic basis for determining how long this productivity improvement would take to “pay for” the time spent in the service merge itself.

* Merging in the events code will increase the technical complexity of the External API service, which sees significantly more change than the events service

  * it will increase dependency of the integration tests on LocalStack, which is currently only used for SQS and SNS
  * it will add a dependency on scheduled task, though this will be offset to some extent by the ability to run these tasks synchronously using API calls
  * it should be noted that it will be possible to create a spring profile that does not enable any of the external-events technical complexity

* Once the External API has access to the notifications database it should be possible to expose this in a controlled manner to external consumers, allowing them to bypass the SQS queue mechanism if required and read notifications directly via API calls.
