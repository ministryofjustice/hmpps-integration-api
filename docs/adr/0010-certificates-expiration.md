# 0010 - Certificates expiration

2024-02-08

## Status

Accepted

## Context
We need to ensure that clients' certificates are always up-to-date. When we generate said certificates, we also upload a copy to a S3 bucket.
The decision is about what the best way to check the certificates' expiration date is, and how to get notified when they are about to expire, so to re-generate them.
There are two main methods we explored:
1. Using AWS resources, namely the Certificate Manager (ACM) for storing; CloudWatch for monitoring; a Lambda for custom actions; and the AWS System Manager Automation.
2. Using a Kubernetes CronJob to run a bash script. CronJobs are currently being used across Cloud Platform and there are multiple examples we can take inspiration from, and we have already a suite of bash scripts inside our codebase.

The bash script will be run within the CronJob yml file. The job is set to run once every day.
The bash script looks into the S3 bucket and retrieves the previously uploaded client certificates. It then checks the expiration date, and if it's less than 90 days away, we get a Slack notification.
We can then proceed to renew the certificates for the impacted clients.

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
