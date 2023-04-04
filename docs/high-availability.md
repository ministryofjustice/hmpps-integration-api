# High availability

The API is hosted in 1 AWS region (London).
Nodes are spread across multiple (up to 3) Availability Zones.

- eu-west-2a
- eu-west-2b
- eu-west-2c

Healthchecks are run by Kubernetes to assess the health of each node. If a node fails 3 times in a row, it is declared unhealthy and a new one put in to take its place.
Kubernetes with auto heal and prevent any corrupt nodes from replacing existing healthy ones.
