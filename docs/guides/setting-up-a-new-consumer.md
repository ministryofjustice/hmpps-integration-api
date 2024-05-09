# Setting up a new consumer

To enable a consumer to access our API, they need:

- a client certificate for mutual TLS with a Subject Distinguished Name(SDN) and Common Name (CN) that matches the authorisation allow list configuration in the application
- an API key

per environment.

## Prerequisites

- [kubectl](https://kubernetes.io/docs/tasks/tools/#kubectl)
- [Access to Cloud Platform’s Kubernetes cluster](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/getting-started/kubectl-config.html#installing-kubectl)

## Create a client certificate

1. As a pre-requisite to create a client certificate by running the script below, please ensure that:
- Select the account you need.SSO registration scopes
- You run `aws configure sso` and follow the prompts to populate the SSO session name [anything], the SSO start URL [https://madetech.awsapps.com/start], the region [eu-west-2] and SSO registration scopes [simply press Enter].
- Authorise the request which will be open on the browser.
- Select the account you want to use, according to what roles you have available. 
- Follow the prompts to populate the client region [eu-west-2], the default output format [json], the CLI profile name [simply press Enter].
- Verify that you have everything ready by opening the config file in the ".aws" directory.
2. Run the [generate-client-certificate.sh](/scripts/client_certificates/generate.sh) script with the name of the environment and client.

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

2. Using [One-Time Secret](https://password.link/en) and email, send the new consumer their:
   1. private key
   2. client certificate
   3. API key
   
## Create new consumer subscriber queue

### Create basic infrastructure
Within the [Cloud Platform Environments GitHub repository](https://github.com/ministryofjustice/cloud-platform-environments/tree/main) and the namespace of the environment:

1. Create a branch.
2. Add new client subscriber terraform file. Example: [event-subscriber-mapps.tf](https://github.com/ministryofjustice/cloud-platform-environments/pull/22091/files#diff-4046866c9398b1db59a427052406a08c2adab45aadbc278f16232157a636f451)
3. Rename client name "mapps" to new client name
4. Add new client filter list secret. exmaple [secret.tf](https://github.com/ministryofjustice/cloud-platform-environments/pull/22091/files#diff-bc13dba50c430d2a667e5b867d2798770e5e8c48697407d93e2febedb3ff46dc)
5. Follow steps 3-8 in [Create an API key](#create-an-api-key) to merge branch to main. 

After the change is merged and applied, you can retrieve client queue name and ARN with the following command:

```bash
kubectl -n hmpps-integration-api-[environment] get secrets [your queue secret name] -o json
# E.g. kubectl -n hmpps-integration-api-dev get secrets event-mapps-queue  -o json 
```
### Using AWS secret for filter Policy
1. Login to the [AWS Console](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/getting-started/accessing-the-cloud-console.html), navigate to Secrets Manager and navigate to the secret created in the previous step by search using the secret description. e.g. MAPPS event filter list Pre-prod
2. Click on the secret and then click on Retrieve secret value. If this is your first time accessing the new secret, you will see an error Failed to get the secret value.
3. Click on Set secret Value, and set the Plaintext value as: {"eventType":["default"]}. Setting filter to default will block subscriber receiving any messages. Event notifier will update the subscriber and AWS secret with actual filter list later.
4. Save the change 
5. Create new [Cloud Platform Environments GitHub repository](https://github.com/ministryofjustice/cloud-platform-environments/tree/main) branch 
6. Update terraform to load the secret value from AWS and update filter_policy value. Follow [Example](https://github.com/ministryofjustice/cloud-platform-environments/pull/22111/files). Note: The name of aws_secretsmanager_secret module has to be same as the secret name created from step 4/5 above. 
7. Follow steps 3-8 in [Create an API key](#create-an-api-key) to merge branch to main. 

## Create a new endpoint for a client

### Create basic infrastructure
Within the [Cloud Platform Environments GitHub repository](https://github.com/ministryofjustice/cloud-platform-environments/tree/main) and the namespace of the environment:

1. Create a branch.
2. Add a new API Gateway resource, a SQS method, a SQS method response, and an integration. Example: [api_gateway.tf](https://github.com/ministryofjustice/cloud-platform-environments/pull/22695/files)
3. Ensure that all the permissions are up-to-date and add a new role and policy for your new resource. Example: [iam.tf](https://github.com/ministryofjustice/cloud-platform-environments/pull/22787/files#diff-a376622fa4a4c2fd9404d5ee4221487259264608a0cbe36b99c150c472558f29)
4. Check that the integration is pointing to the right queue. Example: [api_gateway.tf](https://github.com/ministryofjustice/cloud-platform-environments/pull/22795/files)
5. Deploy and test (do not use Postman, rather use a GET cURL command with "x-api-key" as your header.)
