# Creating an environment

Within the [Cloud Platform Environments repository](https://github.com/ministryofjustice/cloud-platform-environments):

1. Duplicate the `hmpps-integration-api-preprod` namespace and name it with the new environment name.
2. Remove the resources related to API Gateway including values that may get added into a Kubernetes secret and the S3 bucket object for mutual TLS.
3. Update all references to `preprod` with the name of the new environment.
4. Create a pull request to create a namespace for the new environment.

For mutual TLS authentication, we need to generate our own certificate authority (CA) for each environment.

5. Create a private key for our CA.

```bash
openssl genrsa -out [environment]-truststore.key 4096
# E.g. openssl genrsa -out dev-truststore.key 4096
```

6. Create a certificate for our CA.

```bash
openssl req -new -x509 -days 3650 -key [environment]-truststore.key -out [environment]-truststore.pem
# openssl req -new -x509 -days 3650 -key dev-truststore.key -out dev-truststore.pem
```

You will then be asked to enter values for the certificate.

7. Follow the prompts with answers similar to below:

```bash
You are about to be asked to enter information that will be incorporated
into your certificate request.
What you are about to enter is what is called a Distinguished Name or a DN.
There are quite a few fields but you can leave some blank
For some fields there will be a default value,
If you enter '.', the field will be left blank.
-----
Country Name (2 letter code) []:GB
State or Province Name (full name) []:London
Locality Name (eg, city) []:London
Organization Name (eg, company) []:Ministry of Justice
Organizational Unit Name (eg, section) []:
Common Name (eg, fully qualified host name) []:dev.integration-api.hmpps.service.justice.gov.uk
Email Address []:
```

8. Verify the certificate is correct by checking values such as the `CN` in the `Issuer`.

```bash
openssl x509 -in [environment]-truststore.pem -text
# E.g. openssl x509 -in dev-truststore.pem -text
```

9. Create a Kubernetes secret in the new namespace called `mutual-tls-auth` to store the private key and certificate.

```bash
kubectl create secret generic mutual-tls-auth -n hmpps-integration-api-[environment] \
  --from-file='truststore-public-key'=./[environment]-truststore.pem \
  --from-file='truststore-private-key'=./[environment]-truststore.key
```
10. Ask the HMPPS Auth team to create client credentials for the new environment.
    1. Create a Jira ticket on their board like [HAAR-1438](https://dsdmoj.atlassian.net/browse/HAAR-1438).
    2. Send a message on [#hmpps-auth-audit-registers](https://mojdt.slack.com/archives/C02S71KUBED/p1683814409449889) in MOJ's Slack to notify them.

The HMPPS Auth team will create a Kubernetes secret in the new namepsace for us.

Back within the [Cloud Platform Environments repository](https://github.com/ministryofjustice/cloud-platform-environments):

11. Add API Gateway and its related resources to the namespace for the new environment.
12. Update `local.clients` for each person that needs access.
13. Create a pull request to add API Gateway for the new environment.

Within [Sentry](https://ministryofjustice.sentry.io/projects/):

14. Create a project.
    1. Choose Spring Boot as the platform.
    2. Choose "I'll create my own alerts later".
    3. Name the project like `hmpps-integration-[environment]`.
    4. Choose the `#hmpps-integration-api` as the team.
    5. Click on the Create Project button.
15. Make note of the DSN for the new project.
16. Create [an alert similar other environments](https://ministryofjustice.sentry.io/alerts/rules/).

Within [Microsoft Azure](https://portal.azure.com/#home):

17. Go to Application Insights and select the appropriate `nomisapi` for the new environment.
18. Under the Configure section, click on Properties.
19. Make note of the instrumentation key.
20. Create a Kubernetes secret in the new namespace called `other-services` to store the Sentry DSN and Azure Application Insights instrumentation key.

```bash
kubectl create secret generic other-services -n hmpps-integration-api-[environment] \
  --from-literal=sentry='[sentry-dsn]' \
  --from-literal=azure-app-insights='[azure-app-insights-instrumentation-key]'
```

Within [CircleCI](https://app.circleci.com/pipelines/github/ministryofjustice/hmpps-integration-api):

21. Create a context for the new environment by following the [setting up a CircleCI context for deployment](./setting-up-circleci-context-for-deployment.md) guidance.

Within [our API repository](https://github.com/ministryofjustice/hmpps-integration-api/tree/main):

22. Duplicate the `values-preprod.yaml` in the `helm_deploy` directory and name it with the new environment name.
23. Update the `values-[environment].yaml` for the new environment.
24. Duplicate the `application-preprod.yml` in the `src/main/resources` directory and name it with the new environment name.
25. Update the `application-[environment].yml` for the new environment.
26. Update any documentation such as `docs/environments.md`.
27. Add jobs to our CircleCI pipeline in `.circleci/config.yml` to:
    1. Create an image and push to ECR.
    2. Deploy to the new environment.
28. Create a pull request to setup a deployment of the API to the new environment.
29. Merge the pull request and wait for the pipeline to succeed.
30. Using the reporting script, check that all Kubernetes pods are running.

```bash
./scripts/report_kubernetes.sh [environment]
# E.g. ./scripts/report_kubernetes.sh dev
```

31. Generate a client certificate for yourself.

```bash
cd scripts
./generate-client-certificate.sh [environment] [client]
# E.g. ./generate-client-certificate.sh dev bob
```

32. Get your API key for the new environment.

```bash
kubectl -n hmpps-integration-api-[environment] get secrets consumer-api-keys -o json | jq -r '.data.[client]'
# E.g. kubectl -n hmpps-integration-api-dev get secrets consumer-api-keys -o json | jq -r '.data.bob'
```

33. Using your client certificate and API key, test the new environment is working by calling an API endpoint such as `/health` to check it's running and `/persons?first_name=John` to check connection to upstream APIs are working.
