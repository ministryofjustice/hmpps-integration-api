# HMPPS Integration API

[![repo standards badge](https://img.shields.io/badge/dynamic/json?color=blue&style=flat&logo=github&label=MoJ%20Compliant&query=%24.result&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fhmpps-template-kotlin)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-github-repositories.html#hmpps-template-kotlin "Link to report")
[![CircleCI](https://circleci.com/gh/ministryofjustice/hmpps-template-kotlin/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/hmpps-template-kotlin)
[![Docker Repository on Quay](https://quay.io/repository/hmpps/hmpps-template-kotlin/status "Docker Repository on Quay")](https://quay.io/repository/hmpps/hmpps-template-kotlin)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://hmpps-template-kotlin-dev.hmpps.service.justice.gov.uk/webjars/swagger-ui/index.html?configUrl=/v3/api-docs)

## Infrastructure Tech Stack
Information on the technology stack chosen for the HMPPS-Integration-API project

### [Cloud Platform](https://user-guide.cloud-platform.service.justice.gov.uk/#cloud-platform-user-guide)
A hosting platform which offers numerous tools such as logging, monitoring and alerting for our services.

### [Kubernetes](https://kubernetes.io/docs/home/)
Creates 'pods' to host our environment. Manages auto-scaling, load balancing and networking to our application.

### [Docker](https://www.docker.com/)
The API is built into docker images which are deployed to our containers.

### [AWS Elastic Container Registry](https://aws.amazon.com/ecr/)
Our built artefacts are stored within an ECR. The CI/CD pipeline will store and retrieve them from here as required.

### [Circle CI](https://circleci.com/developer)
_This may yet be switched for Github Actions depending on investigation_

### [AWS](https://aws.amazon.com/)
Services utilise AWS features through cloud platform.

Used for our build platform, responsible for executing workflows to build, validate, test and deploy our project.


## Useful Commands
Commands that you might find useful when working with the environment.

### kubectl

To report on all resources for an environment, run the script:
```
./scripts/report-kubernetes.sh <namespace>
# E.g ./scripts/report-kubernetes.sh development
```

Alternatively, the commands below yield information on specific resources.

To get ingress information for a namespace:
```
kubectl get ingress -n <namespace>
```

To get a list of all services for a namespace:
```
kubectl get service -n <namespace>
```

To get a list of all deployments for a namespace:
```
kubectl get deployment -n <namespace>
```

To get a list of all pods for a namespace:
```
kubectl get pod -n <namespace>
```

To get detailed information on a specific pod:
```
kubectl describe pod <podname> -n <namespace>
```

To view logs of a pod:
```
kubectl logs <pod-name> -n <namespace>
```

To perform a command within a pod:
```
kubectl exec <pod-name> -c <container-name> -n <namespace> <command>
#E.g. kubectl exec hmpps-integration-api-5b8f4f9699-wbwgf -c hmpps-integration-api -n hmpps-integration-api-development -- curl http://localhost:8080/
```

To delete all ingress, services, pods and deployments:
```
kubectl delete pod,svc,deployment,ingress --all -n <namespace>
```

### aws

To list images in the ECR repository:
```
aws ecr describe-images --repository-name=hmpps-integration-api-team/hmpps-integration-api-<environment>-ecr
```
