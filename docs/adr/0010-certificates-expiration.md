# 0009 - Authorisation at application level

2024-02-07

## Status

Accepted

## Context
We need to ensure that clients' certificates are always up-to-date. When we generate said certificates, we also upload a copy to a S3 bucket.
The decision is about what the best way to check the certificates' expiration date is, and how to get notified when they are about to expire, so to re-generate them.
There are two main methods we explored:
1. Using AWS resources, namely the Certificate Manager (ACM) for storing; CloudWatch for monitoring; a Lambda for custom actions; and the AWS System Manager Automation.
2. Using a Kubernetes CronJob to run a bash script.

## Decision

We decided against the for the first option because:
- Using AWS would prove difficult because of the nature of the certificates (AWS CM only allows for a certain type of certificate to be imported, and ours wouldn't be allowed);
- The costs involved in using AWS resources;
- We don't need to write a custom-made lambda.

We decided to go for the second option because:
- We already have a way to be notified via Slack, and it's an easy way to reuse existing code;
- We have more control on potential new changes as it's a simple script (e.g. if we need to increase or decrease the expiration period);
- It's the most cost-effective option.


## Consequences
- Our GitHub repository will contain the script to run inside the CronJob (called check_certificate_expiry.sh).
