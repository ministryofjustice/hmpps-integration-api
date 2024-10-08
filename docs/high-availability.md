# High availability

## Availability Zones

The API is hosted in 1 AWS region (London).
Nodes are spread across multiple (up to 3) Availability Zones.

- eu-west-2a
- eu-west-2b
- eu-west-2c

## Deployment health checks

Health checks are run by Kubernetes to assess the health of each node. If a node fails 3 times in a row, it is declared unhealthy and a new one put in to take its place.
Kubernetes with auto heal and prevent any corrupt nodes from replacing existing healthy ones.

## Heartbeat

In addition to health checks, there is also a "Heartbeat" which ensures pods are healthy after a deployment.
Please see [Heartbeat](./monitoring-and-alerting/heartbeat.md)

## Distributed Denial of Service (DDOS) protection

MOJ Cloud Platform implement [AWS Shield](https://aws.amazon.com/shield/), which will guard against any DDOS attacks.
As a second layer of defense we've added rate limiting to our nginx ingress configuration [here](https://github.com/ministryofjustice/hmpps-integration-api/blob/main/helm_deploy/hmpps-integration-api/values.yaml).
The annotation is `nginx.ingress.kubernetes.io/limit-rps` and set to a maximum of 100 requests per second.
