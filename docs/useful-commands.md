# Useful commands

## kubectl

To report on all resources for an environment, run the script:

```bash
./scripts/report_kubernetes.sh <environment>
# E.g ./scripts/report_kubernetes.sh development
```

<details>
  <summary>Alternatively, the commands to yield information on specific resources.</summary>
  <br>

To get ingress information for a namespace:

```bash
kubectl get ingress -n <namespace>
```

To get a list of all services for a namespace:

```bash
kubectl get service -n <namespace>
```

To get a list of all deployments for a namespace:

```bash
kubectl get deployment -n <namespace>
```

To get a list of all pods for a namespace:

```bash
kubectl get pod -n <namespace>
```

To get detailed information on a specific pod:

```bash
kubectl describe pod <podname> -n <namespace>
```

To view logs of a pod:

```bash
kubectl logs <pod-name> -n <namespace>
```

To perform a command within a pod:

```bash
kubectl exec <pod-name> -c <container-name> -n <namespace> <command>
# E.g. kubectl exec hmpps-integration-api-5b8f4f9699-wbwgf -c hmpps-integration-api -n hmpps-integration-api-development -- curl http://localhost:8080/
```

To delete all ingress, services, pods and deployments:

```bash
kubectl delete pod,svc,deployment,ingress --all -n <namespace>
```

</details>

## aws

To list images in the ECR repository:

```bash
aws ecr describe-images --repository-name=hmpps-integration-api-admin-team/hmpps-integration-api-<environment>-ecr
```
