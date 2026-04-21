## The role of events code

The events served from this service are primarily intended to be used to let you know when to invalidate a cache. The events are:

- Minimal - they do not contain data beyond what is needed to refresh your cache (e.g. the URL).
- Mapped directly to API endpoints. An event is only provided to you if you have access to the corresponding endpoint.

For example, if you have access to the "Get a person's name" endpoint (`v1/persons/{hmppsId}/name`), and a new person is created in the upstream systems, you will receive a `PERSON_NAME_CHANGED` event for an HMPPS ID you have not seen before. You will not receive a new person event.

This also means that if you have access to multiple endpoints, a single action (such as a new person being created in the system) may result in you receiving multiple events.

### How it works

At a high level, this service

1. Listens for HMPPS Domain Events
2. Transforms them into HMPPS Integration Events
3. Puts the Integration Event on the Integration Event Topic

Consumers who want to receive Integration Events, [will need an SQS queue and Subscription to the Integration Events Topic created](https://github.com/ministryofjustice/hmpps-integration-api/blob/main/docs/guides/setting-up-a-new-consumer.md#create-new-consumer-subscriber-queue-for-events). This will provide them with a queue that receives events when they are put on the Integration Event topic

This project has three asynchronous processes:

#### 1. Update filter policies - On Deploy

To restrict the events that a consumer receives, the SNS subscription filter policy for each queue is updated every deploy of the code. To do this, we

- Update the SNS subscription filter policy.

We set the filter policies to only allow the events that

- Correspond to endpoints they have access to.
- Match the filters they have on the Integration API (if any).

#### 2. Listen for HMPPS Domain Events

Whenever a Domain event is received, we convert it to the corresponding Integration Events and insert them into the database (a single Domain event can cause multiple Integration Events). In the case that the new Integration Event is a duplicate, we update the existing event.

#### 3. Send HMPPS Integration Events - Every 10 seconds

Search the database for events older than 5 minutes. Add them to the Integration Events Topic and then delete them.
