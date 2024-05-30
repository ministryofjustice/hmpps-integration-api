# Renew mutual TLS server certificate

As we mention in our document [creating-an-environment.md](./creating-an-environment.md), for mutual TLS authentication, we need to generate our own certificate authority (CA) for each environment.
This means that while the CA itself has a long expiration date of 10 years, the server certificate will expire after **one** year by default.
The same is true for the client certificate which we generate when we run the `generate.sh` script in our codebase.

When you need to renew the mutual TLS server certificate, you need to:

1. create a new certificate in Cloud Platform's Terraform in the `api-gateway.tf` file.

```terraform
resource "aws_api_gateway_client_certificate" "api_gateway_client" {
  description = "Client certificate presented to the backend API expires 30/05/2025"
}
```

2. Then, you need to update the client_certificate_id's value in the aws_api_gateway_stage resource.

```terraform
resource "aws_api_gateway_stage" "main" {
  deployment_id         = aws_api_gateway_deployment.main.id
  rest_api_id           = aws_api_gateway_rest_api.api_gateway.id
  stage_name            = var.namespace
  client_certificate_id = aws_api_gateway_client_certificate.api_gateway_client.id
}
```

3. Then, go to the `kubernetes_secrets.tf` file and update the value of the client_certificate_auth secret using the newly generated certificate.

```terraform
resource "kubernetes_secret" "client_certificate_auth" {
  metadata {
    name      = "client-certificate-auth"
    namespace = var.namespace
  }

  data = {
    "ca.crt" = aws_api_gateway_client_certificate.api_gateway_client.pem_encoded_certificate
  }
}
```
