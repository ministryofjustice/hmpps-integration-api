# Setting up a new consumer

To enable a consumer to access our API, they need:

- a client certificate for mutual TLS with a Subject Distinguished Name(SDN) and Common Name (CN) that matches the authorisation allow list configuration in the application
- an API key

per environment.

## Prerequisites

- [kubectl](https://kubernetes.io/docs/tasks/tools/#kubectl)
- [Access to Cloud Platform’s Kubernetes cluster](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/getting-started/kubectl-config.html#installing-kubectl)

## Create a client certificate

1. Run the [generate-client-certificate.sh](/scripts/client_certificates/generate.sh) script with the name of the environment and client.

```bash
make generate-client-certificate
```

This will output three files in the ./scripts/client_certificates directory:

- a private key e.g. `dev-nhs-client.key`
- a certificate signing request (CSR) e.g. `dev-nhs-client.csr`
- a client certificate (public key) e.g. `dev-nhs-client.pem`

The private key and public key can be shared with the consumer.
The private key must be kept secret and the public key can be shared freely.

## Create an API key

Within the [Cloud Platform Environments GitHub repository](https://github.com/ministryofjustice/cloud-platform-environments/tree/main) and the namespace of the environment:

1. Create a branch.
2. Add the client to the `clients` local in the [locals.tf](https://github.com/ministryofjustice/cloud-platform-environments/blob/aa34840fcc4d20b10e8d5785cf0039eefe411113/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-integration-api-dev/resources/locals.tf#L13).

This local is used for the [API key resource](https://github.com/ministryofjustice/cloud-platform-environments/blob/8d1506b8cb53511e075602910cb47eef4a8759d1/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-integration-api-dev/resources/api_gateway.tf#L144-L147)
and for the [resource to connect the API key to a usage plan](https://github.com/ministryofjustice/cloud-platform-environments/blob/8d1506b8cb53511e075602910cb47eef4a8759d1/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-integration-api-dev/resources/api_gateway.tf#L158-L164), so when `clients`
is updated, these resources will update.

3. Commit, push and make a pull request.
4. Wait for all the CI checks to pass.
5. Check the Terraform plan in Concourse by following the Details link under the `concourse-ci/status` status check and clicking on the task `plan-environments`.

The changes should include the additions of a new API key and usage plan key as well
as an update to the Kubernetes secret for storing our API keys.

6. Ask the Cloud Platform team to review the pull request in [their Slack channel](https://moj.enterprise.slack.com/archives/C57UPMZLY).
7. Once the pull request has been approved, merge it.

After some initial CI checks, a comment will be added to the pull request with a link
to the Concourse run that will be performing Terraform apply to the namespace. (You'll receive an email when it's been added.)

8. Follow the Concourse build link and check that the Terraform apply succeeds.

The API key will be automatically generated and saved as a Kubernetes secret for future reference.

You can retrieve this API key with the following command:

```bash
kubectl -n hmpps-integration-api-[environment] get secrets consumer-api-keys -o json | jq -r '.data.[client]'
# E.g. kubectl -n hmpps-integration-api-dev get secrets consumer-api-keys -o json | jq -r '.data.bob'
```

## Configure allowed endpoints for the consumer

Add your client common name to the ./src/main/resources/application-[environment].yaml, listing the paths that the new client is allowed to consume.
It is important that the name of the client matches the common name exactly.

To view the common name of the client certificate that was just generated, run:

```bash
openssl x509 -in ./scripts/client_certificates/[environment]-[consumer]-client.pem -text |grep Subject |grep CN
```

## Send the credentials to the consumer

1. Retrieve the API key for the new consumer using Kubectl.

```bash
kubectl -n hmpps-integration-api-<environment> get secrets consumer-api-keys -o json | jq -r '.data.<client>' | base64 -d
# E.g. kubectl -n hmpps-integration-api-dev get secrets consumer-api-keys -o json | jq -r '.data.dev' | base64 -d
```

2. Using [One-Time Secret](https://onetimesecret.com/) and email, send the new consumer their:
   1. private key
   2. client certificate
   3. API key
