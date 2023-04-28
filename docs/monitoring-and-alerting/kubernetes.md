# Kubernetes Logs
Each pod the service is runs on has its own logs. These logs contain information which pertains to the instance of the API
running within the pod. The sort of information that can be obtained from the Kubernetes logs is as follows:
- Application and server hosting information such as local ports, versions and active profiles.
- Incoming requests (does not contain sensitive information) i.e. request type, request URL and request body.

> Since traffic to the pods is load balanced, multiple logs may need to be checked for each pod the service runs on. For
> this reason we'd recommend using [Kibana](kibana.md) when numerous pods are running.

## Setup
Ensure you are able to access Cloud Platform's Kubernetes cluster via the Kubernetes CLI. This can be done by running
`kubectl get namespaces`. A successful call will return a list of all namespaces within the cluster.

**If not** Follow Cloud Platform's [documentation](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/getting-started/kubectl-config.html) on how to set this up.

## Steps
1. Get the unique names of the pods that are running.
```
kubectl get pods -n hmpps-integration-api-<environment>
```
2. Using the name of the pods, you can retrieve the logs via the following command:
```
kubectl logs -n hmpps-integration-api-<environment> <podname>
```
If successful, your terminal will display the logs for this pod.
> You can optionally follow the logs in your terminal by appending the `-f` flag onto the end of the logs command.

### Example Use Case
A consumer is getting unexpected data from the API, in such a scenario we can obtain the request details from the log and
reproduce it ourselves.