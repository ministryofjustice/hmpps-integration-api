# Security

This service is accessed exclusively through the API and has no other user interfaces.
Onboarding new clients is a manual process and there is no other way to gain access credentials.
All in-flight requests are encrypted and sent over HTTPS over the public internet. Any data at rest is encrypted with AWS [Key Management Service (KMS)](https://aws.amazon.com/kms/).

IP Restrictions are in place and will prevent the majority of unauthorised access attempts. We do not currently have AWS [WAF](https://aws.amazon.com/waf/) applied on our ingress.
All secrets are stored as Kubernetes secrets.

The tokens provided by HMPPS Auth (to access upstream APIs such as NOMIS) by default last 20 minutes before refresh.
These tokens have limited read-only access to accomplish only tasks required by the API.

Below is a list of protected resources that make up the service, and access levels to those resources.

| Access               | Controlled by                                                         | Limited to                      |
| -------------------- | --------------------------------------------------------------------- | ------------------------------- |
| API                  | API Keys, Certificates                                                | Registered External consumers   |
| AWS Account (Live)   | IAM users with access credentials and two-factor authentication (2FA) | Members of Cloud Platform team  |
| Namespace Secrets    | GitHub teams                                                          | HMPPS Integration API engineers |
| S3 Bucket            | IAM policy, Bucket Policy                                             | HMPPS Integration API engineers |
| ECR                  | IAM policy, IAM User                                                  | HMPPS Integration API engineers |
| API Gateway          | IAM policy, IAM User                                                  | HMPPS Integration API engineers |
| Code (read)          | GitHub                                                                | Public                          |
| Code (write)         | GitHub - specific teams                                               | HMPPS Integration API engineers |
| Deployment (live)    | CircleCI with approval step                                           | Approval - Product Owners       |
| CircleCI             | Via GitHub account                                                    | HMPPS Integration API engineers |
| Sentry               | Via GitHub account                                                    | All engineers in MOJ            |
| Application Insights | Via GitHub account                                                    | All engineers in MOJ            |
| Kibana               | Via GitHub account                                                    | All engineers in MOJ            |
