# 0008 - Use certificates in production

2024-01-08

## Status

Accepted

## Context

The decision to utilize certificates for securing our project was made during the setup of new consumers accessing our API. The primary goals were to establish mutual TLS authentication and ensure a secure authorization mechanism for API access.

## Decision

Certificates, specifically client certificates, are chosen as the primary security mechanism for our project. This decision is underpinned by the advantages offered by certificates, especially in the context of achieving mutual Transport Layer Security (TLS) authentication.

The decision to employ certificates for project security, coupled with mutual TLS, is driven by the following benefits:
- Strong Authentication: Certificates provide a robust mechanism for authenticating consumers, mitigating the risk of unauthorized access.
- Mutual TLS Security: The use of mutual TLS ensures that both the server and the client are mutually authenticated, establishing a secure and trusted communication channel.
- Fine-Grained Authorization: Combining client certificates with API keys enables a two-tiered authorization approach, allowing for granular control over consumer access to endpoints.
- Consistency Across Environments: The centralized management of API keys through the Cloud Platform's GitHub repository ensures consistency and traceability across different environments.

### Example

1. Client Certificate Generation
   - Client certificates are created with a Subject Distinguished Name (SDN) and Common Name (CN) that aligns with the authorization allow list configuration in the application.
   - The use of certificates enhances the overall security posture by providing a reliable means of authenticating consumers.
2. API Key Creation
    - API keys are coupled with client certificates, creating a dual-layered security approach. This ensures that consumers possess both a valid certificate and an associated API key for authorization.
    - The combination of certificates and API keys establishes a robust authentication and authorization framework.
3. Consumer Configuration
   - The inclusion of the client's common name in the application configuration file allows fine-grained control over the endpoints accessible to the consumer.
   - Certificates, with their embedded common names, provide a seamless way to enforce access controls for individual consumers.
4. Credential Distribution
    - Distributing private keys, client certificates, and API keys securely to consumers strengthens the overall security architecture.
    - The combination of One-Time Secret and email ensures confidentiality during the transmission of sensitive credentials.

## Consequences

- Consumers are required to manage and securely store their private keys.
- The Cloud Platform's GitHub repository is the central source for managing API keys, ensuring consistency and traceability across environments.
- Certificates have a finite lifespan, and their expiration must be actively managed. Failure to renew certificates in a timely manner can lead to service disruptions and potential security vulnerabilities.
- Consumers need to be educated on the proper handling, storage, and renewal of certificates.