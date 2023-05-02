# Azure Application Insights
Data related to the quantities of requests made against our API as well as response times. These logs can also be used to
identify bottlenecks in performance and to determine endpoint metric data such as the most commonly used endpoints.

## Setup
An HMPPS Azure account will be required to access application insights, please follow the [HMPPS documentation](https://dsdmoj.atlassian.net/wiki/spaces/DSTT/pages/3897131056/DSO+Self-service+-+create+Azure+account).
During this process you will be required to add an entry to a configuration file, it's recommended that you use an entry
from someone else on the HMPPS Integration API as a template for your own.

[Here's](https://github.com/ministryofjustice/dso-infra-azure-ad/pull/1053) an example of a previous PR.

## Steps
1. Log in to the [Application Insights dashboard](https://portal.azure.com/#view/HubsExtension/BrowseResource/resourceType/microsoft.insights%2Fcomponents)
2. Select an environment
    1. Development: `nomisapi-t3`
    2. Pre-Production: `nomisapi-preprod`
    3. Production: `nomisapi-prod`
4. Select `Logs` within the window
5. Either select a preset query, or close the window to write your own
6. Filter the query on the following `| where cloud_RoleName = 'hmpps-integration-api`. This will restrict the query to
   only display data for the HMPPS Integration API.

## Example Use Case
We'd like to check how a particular endpoint is performing. Using a query such as the `Operations Performance` query. 
We can look at average response times for a given endpoint. This is beneficial as it removes any one user's networking
as a factor and looks at the full data set of consumers.