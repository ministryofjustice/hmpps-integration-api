# Grafana
Provides a graph-based dashboard view containing information on the performance of the service. The level at which this
is provided is for the whole namespace (development, pre-production or production). Details such as quantities of requests
CPU usage, memory usage and networking details can be located in various graphs. The period of data shown within the 
dashboard can also be configured.

## Setup
1. If you're a member of the Ministry of Justice Git Organisation you should be able to log in to [Grafana](https://grafana.live.cloud-platform.service.justice.gov.uk/?orgId=1).

## Steps
1. Follow Cloud Platform's [documentation](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/monitoring-an-app/prometheus.html#grafana)
2. Access our [dashboard](https://grafana.live.cloud-platform.service.justice.gov.uk/d/golden-signals/golden-signals?orgId=1&var-namespace=hmpps-integration-api-development&var-service=hmpps-integration-api)
3. Change the environment using the namespace drop-down menu on the top left (if necessary)

## Example Use Case
We've noticed that the API is responding slowly to specific requests. The Grafana metrics will allow us to check the
resources on the pods to ensure we're not reaching hardware limits.