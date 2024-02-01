# Audit

This section contains information as to where relevant areas of logging can be located in the event of an audit.

## Data logs

The HMPPS Integration API doesn't store any data itself; Further to this, It is also not capable of creating or modifying
the data it works with. The API is a single point of contact which will query upstream system's. It will then consolidate,
standardised and pass the resulting data back to the requester.

## Deployment logs

Logs of each deployment can be located within the following areas:

### [Git](https://github.com/ministryofjustice/hmpps-integration-api)

A list of the changes made to project can be found through the history of commits and pull requests.

### [CircleCI](https://app.circleci.com/pipelines/github/ministryofjustice/hmpps-integration-api)

Build and deployment information. This is on a per-build basis.

## Access logs

Logs that contain access to various endpoints throughout our system can be located through Elasticsearch. Our interface
to Elasticsearch is a tool called [Kibana](https://kibana.cloud-platform.service.justice.gov.uk/).

Logs contained within Elasticsearch will not contain any sensitive data. The logs will contain a request details as well
as a timestamp as to when the record was accessed; This allows identification of the record. This information
does not include any response data.

Elasticsearch logs have a retention period of 30 days. See the [Application Log Collection and Storage](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/logging-an-app/log-collection-and-storage.html)
section of the Cloud Platform documentation for more detail.

> For further information on how to access our monitoring and alerting systems, please visit our [Monitoring and Alerting](monitoring-and-alerting/README.md) page.
