# HMPPS Integration API

[![repo standards badge](https://img.shields.io/badge/dynamic/json?color=blue&style=flat&logo=github&label=MoJ%20Compliant&query=%24.result&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fhmpps-integration-api)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-github-repositories.html#hmpps-integration-api "Link to report")
[![CircleCI](https://circleci.com/gh/ministryofjustice/hmpps-integration-api/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/hmpps-integration-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://hmpps-integration-api-development.apps.live.cloud-platform.service.justice.gov.uk/swagger-ui/index.html)

## Contents

- [Getting started](#getting-started)
  - [Using IntelliJ IDEA](#using-intellij-idea)
- [Usage](#usage)
  - [Running the application](#running-the-application)
  - [Running the tests](#running-the-tests)
  - [Running the linter](#running-the-linter)
  - [Running all checks](#running-all-checks)
- [Infrastructure Tech Stack](#infrastructure-tech-stack)
  - [Cloud Platform](#cloud-platform)
  - [Kubernetes](#kubernetes)
  - [Docker](#docker)
  - [AWS Elastic Container Registry](#aws-elastic-container-registry)
  - [CircleCI](#circleci)
  - [AWS](#aws)
- [Useful Commands](#useful-commands)
  - [kubectl](#kubectl)
  - [aws](#aws-1)

## Getting started

### Using IntelliJ IDEA

When using an IDE like [IntelliJ IDEA](https://www.jetbrains.com/idea/), getting started is very simple as it will handle installing the required Java SDK and [Gradle](https://gradle.org/) versions. The following are the steps for using IntelliJ but other IDEs will prove similar.

1. Clone the repo.

```bash
git clone git@github.com:ministryofjustice/hmpps-integration-api.git
```

1. Launch IntelliJ and open the `hmpps-integration-api` project by navigating to the location of the repository.

Upon opening the project, IntelliJ will begin downloading and installing necessary dependencies which may take a few minutes.

## Usage

### Running the application

To run the application using IntelliJ:

1. Select the `HmppsIntegrationApi` run configuration file.
2. Click the run button.

To run the application using the command line:

```bash
./gradlew bootRun
```

### Running the tests

The testing framework used in this project is [Kotest](https://kotest.io/).

To run the tests using IntelliJ:

1. Install the [Kotest IntelliJ plugin](https://kotest.io/docs/intellij/intellij-plugin.html).

This provides the ability to easily run a test as it provides run buttons (gutter icons) next to each test and test file.

2. Click the run button beside a test or test file.

To run the application using the command line:

```bash
./gradlew test
```

### Running the linter

To lint the code using [Ktlint](https://pinterest.github.io/ktlint/):

```bash
./gradlew ktlintCheck
```

### Running all checks

To run all the tests and linting:

```bash
./gradlew check
```

## Infrastructure Tech Stack
Information on the technology stack chosen for the HMPPS-Integration-API project

### [Cloud Platform](https://user-guide.cloud-platform.service.justice.gov.uk/#cloud-platform-user-guide)
MoJ's cloud hosting platform built on top of AWS. which offers numerous tools such as logging, monitoring and alerting for our services.

### [Kubernetes](https://kubernetes.io/docs/home/)
Creates 'pods' to host our environment. Manages auto-scaling, load balancing and networking to our application.

### [Docker](https://www.docker.com/)
The API is built into docker images which are deployed to our containers.

### [AWS Elastic Container Registry](https://aws.amazon.com/ecr/)
Our built artefacts are stored within an ECR. The CI/CD pipeline will store and retrieve them from here as required.

### [CircleCI](https://circleci.com/developer)
_This may yet be switched for GitHub Actions depending on investigation_

Used for our build platform, responsible for executing workflows to build, validate, test and deploy our project.

### [AWS](https://aws.amazon.com/)
Services utilise AWS features through Cloud Platform.

## Useful Commands
Commands that you might find useful when working with the environment.

### kubectl

To report on all resources for an environment, run the script:
```bash
./scripts/report-kubernetes.sh <environment>
# E.g ./scripts/report-kubernetes.sh development
```

Alternatively, the commands below yield information on specific resources.

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

### aws

To list images in the ECR repository:
```bash
aws ecr describe-images --repository-name=hmpps-integration-api-team/hmpps-integration-api-<environment>-ecr
```
