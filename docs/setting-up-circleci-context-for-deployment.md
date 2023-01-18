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
- `ECR_ENDPOINT`

## Prerequisites

- Access to CircleCI
- [Cloud Platform CLI](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/getting-started/cloud-platform-cli.html#cloud-platform-cli)
- [kubectl](https://kubernetes.io/docs/tasks/tools/#kubectl)
- [Access to Cloud Platform’s Kubernetes cluster](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/getting-started/kubectl-config.html#installing-kubectl)
- [ECR repository for the environment](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/getting-started/ecr-setup.html)
- [Service account for CircleCI for the environment](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/deploying-an-app/using-circleci-for-continuous-deployment.html#creating-a-service-account-for-circleci)

## Creating a CircleCI context

1. Go to [Contexts for the Ministry of Justice's CircleCI organisation](https://app.circleci.com/settings/organization/github/ministryofjustice/contexts?return-to=https%3A%2F%2Fapp.circleci.com%2Fprojects%2Fproject-dashboard%2Fgithub%2Fministryofjustice%2F).
2. Click on "Create Context" button.
3. Name the context using the name of this project and the name of the
   environment: `hmpps-integration-api-<environment>` e.g. `hmpps-integration-api-development`.
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
   the response of step 6.
9. Add an environment variable called `KUBE_ENV_NAMESPACE` and set the value to the Kubernetes namespace for the
   environment e.g. `hmpps-integration-api-development`.
10. Add an environment variable called `KUBE_ENV_NAME` and set the value
    to `DF366E49809688A3B16EEC29707D8C09.gr7.eu-west-2.eks.amazonaws.com`.
11. Add an environment variable called `KUBE_ENV_API` and set the value
    to `https://DF366E49809688A3B16EEC29707D8C09.gr7.eu-west-2.eks.amazonaws.com`.

12. Using the command-line, list the name of all the secrets within the Kubernetes namespace for the environment.

```bash
kubectl get secrets -n hmpps-integration-api-<environment>
# E.g. kubectl get secrets -n hmpps-integration-api-development
```

13. Using the name of the CircleCI service account secret, retrieve the token for it.

```bash
cloud-platform decode-secret -n hmpps-integration-api-<environment> -s <circleci-token-secret-name> | jq -r '.data."token"'
# E.g. cloud-platform decode-secret -n hmpps-integration-api-development -s circleci-token-z123 | jq -r '.data."token"'
```

14. Add an environment variable called `KUBE_ENV_TOKEN` and set the value to the response of the previous command.
15. Using the command-line, retrieve the CA certificate for the CircleCI service account.

```bash
kubectl -n hmpps-integration-api-<environment> get secrets <circleci-token-secret-name> -o json | jq -r '.data."ca.crt"'
# E.g. kubectl -n hmpps-integration-api-development get secrets circleci-token-z123 -o json | jq -r '.data."ca.crt"'
```

16. Add an environment variable called `KUBE_ENV_CACERT` and set the value to the response of the previous command.
17. Add an environment variable called `ECR_ENDPOINT` and set the value to the ECR name e.g. `754256621582.dkr.ecr.eu-west-2.amazonaws.com/hmpps-integration-api-admin-team/hmpps-integration-api-development-ecr`.
