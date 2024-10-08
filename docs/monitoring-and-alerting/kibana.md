# Kibana
Logs based on ElasticSearch are available through the tool Kibana. Usage of Kibana is recommended where you want to look 
at logs for multiple pods and/or environments at once as the logs for any given environment includes all pod logs. 
Using Kibana can alleviate the requirement of monitoring pod logs individually as is necessary with the
[Kubernetes Logs](kubernetes.md). Kibana also allows time based filtering of logs.

The information you'll find within the Kibana logs is any information that has been logged to the standard output of each
pod.

## Setup
Follow Cloud Platform's [documentation](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/logging-an-app/access-logs.html#accessing-application-log-data)

## Steps
[hmpps-integration-api-dashboard](https://kibana.cloud-platform.service.justice.gov.uk/_plugin/kibana/goto/c78b76502b288a271254817cc39db78f) is a direct link to the dashboard which the steps below lead you to.
1. Click the hamburger button on the top left
2. Go to Dashboard
3. Search for 'hmpps-integration-api-dashboard'
4. Select 'hmpps-integration-api-dashboard'

## Example Use Case
You've made a request on the API but aren't sure which pod you've been directed to as there are a number running. Using
Kibana will allow you to see the logs regardless of the pod they're running on.