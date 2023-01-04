# Setting up a CircleCI context for deployment

To set up deployment for an environment using [CircleCI](https://circleci.com/), a
new [context](https://circleci.com/docs/contexts/) must be created for it that contains the following:

- `AWS_DEFAULT_REGION`
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `KUBE_ENV_TOKEN`
- `KUBE_ENV_CACERT`
- `KUBE_ENV_NAME`
- `KUBE_ENV_NAMESPACE`
- `KUBE_ENV_API`

## Prerequisites

- [Cloud Platform CLI](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/getting-started/cloud-platform-cli.html#cloud-platform-cli)
- [kubectl](https://kubernetes.io/docs/tasks/tools/#kubectl)
- [Access to Cloud Platform’s Kubernetes cluster](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/getting-started/kubectl-config.html#installing-kubectl)
- [ECR repository for the environment](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/getting-started/ecr-setup.html)
- [Service account for CircleCI for the environment](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/deploying-an-app/using-circleci-for-continuous-deployment.html#creating-a-service-account-for-circleci)
- Access to CircleCI

## Creating a CircleCI context

1. Go
   to [Contexts for the Ministry of Justice's CircleCI organisation](https://app.circleci.com/settings/organization/github/ministryofjustice/contexts?return-to=https%3A%2F%2Fapp.circleci.com%2Fprojects%2Fproject-dashboard%2Fgithub%2Fministryofjustice%2F)
   .
2. Click on "Create Context" button.
3. Name the context using the name of this project and the name of the
   environment: `hmpps-integration-api-<environment>`.
4. Click on "Add Environment Variable" button.
5. Add an environment variable called `AWS_DEFAULT_REGION` and set the value to `eu-west-2`.
6. Using the command-line, retrieve the AWS credentials to access the ECR repository for the environment.

```bash
cloud-platform decode-secret -n hmpps-integration-api-<environment> -s ecr-repo-hmpps-integration-api-<environment>
# E.g. cloud-platform decode-secret -n hmpps-integration-api-development -s ecr-repo-hmpps-integration-api-development
```

7. Add an environment variable called `AWS_ACCESS_KEY_ID` and set the value to value in the `access_key_id` in the
   response of the previous command.
8. Add an environment variable called `AWS_SECRET_ACCESS_KEY` and set the value to value in the `secret_access_key` in
   the response of the previous command.
9. Using the command-line, list the name of all the secrets within the Kubernetes namespace for the environment.

```bash
kubectl get secrets -n hmpps-integration-api-<environment>
# E.g. kubectl get secrets -n hmpps-integration-api-development 
```

```bash
cloud-platform decode-secret -n hmpps-integration-api-<environment> -s ecr-repo-hmpps-integration-api-<environment>
# E.g. cloud-platform decode-secret -n hmpps-integration-api-development -s ecr-repo-hmpps-integration-api-development
```

## `KUBE_ENV_TOKEN`

## `KUBE_ENV_CACERT`

## `KUBE_ENV_NAME`

## `KUBE_ENV_NAMESPACE`

## `KUBE_ENV_API`
