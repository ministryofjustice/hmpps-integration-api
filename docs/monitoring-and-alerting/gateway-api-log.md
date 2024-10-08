# Gateway API Log
Data related to the identity of the caller, the request, and the response of requests made against our API. These logs can be helpful for monitoring, troubleshooting, and analyzing the usage of the API.

## Setup
If you're a member of the GitHub team "hmpps-integration-api" you should be able to view log in AWS Console

## Steps
1. Follow Cloud Platform's [documentation](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/getting-started/accessing-the-cloud-console.html#login-to-the-aws-console) to log in AWS Console
2. Select CloudWatch service and go to Log groups [LogGroups](https://eu-west-2.console.aws.amazon.com/cloudwatch/home?region=eu-west-2#logsV2:log-groups)
3. Search log groups by name. e.g. integration-api-dev
4. Click on log gtroup name and go to Log streams tab
5. Choose a log stream by click on the name

## Example Use Case
We'd like to debug a request. Gateway API execution log can provide details on the request payload, the endpoint and response of the request. This helps in troubleshooting and identifying the root cause of issues.
