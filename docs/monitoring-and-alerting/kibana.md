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
[hmpps-integration-api-dashboard](https://kibana.cloud-platform.service.justice.gov.uk/_plugin/kibana/app/dashboards#/view/87dccad0-e503-11ed-a7a3-8d8f1c5a54a7?_g=(filters%3A!()%2CrefreshInterval%3A(pause%3A!t%2Cvalue%3A0)%2Ctime%3A(from%3Anow-1h%2Cto%3Anow)) is a direct link to the dashboard which the steps below lead you to.
1. Click the hamburger button on the top left
2. Go to Dashboard
3. Search for 'hmpps-integration-api-dashboard'
4. Select 'hmpps-integration-api-dashboard'

## Example Use Case
You've made a request on the API but aren't sure which pod you've been directed to as there are a number running. Using
Kibana will allow you to see the logs regardless of the pod they're running on.