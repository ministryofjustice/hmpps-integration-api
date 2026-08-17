#!/usr/bin/env bash
##
## Push messages contained in a file to an hmpps-integration-api SQS queue
##
## This scripts starts an ephemeral pod in the environments namespace and pushes messages from the provided file
##
## Usage:
## /push-messages.sh -e <env> -q <queueName> -f <fileName>
## example input file:
## {"Type":"Notification","MessageId":"...","TopicArn":"...","Message":"{\"eventId\":166,\"hmppsId\":\"A123456\",\"eventType\":\"PERSON_STATUS_CHANGED\",\"prisonId\":\"TRN\",\"url\":\"https://dev.integration-api.hmpps.service.justice.gov.uk/v1/persons/A123456\",\"lastModifiedDateTime\":\"2017-03-15T15:47:51.000000\"}","Timestamp":"2025-09-24T09:39:24.000Z","SignatureVersion":"1","Signature":"...","SigningCertURL":"...","UnsubscribeURL":"...","MessageAttributes":{"prisonId":{"Type":"String","Value":"TRN"},"eventType":{"Type":"String","Value":"PERSON_STATUS_CHANGED"}}}
## {"Type":"Notification","MessageId":"...","TopicArn":"...","Message":"{\"eventId\":166,\"hmppsId\":\"A123457\",\"eventType\":\"PERSON_STATUS_CHANGED\",\"prisonId\":\"TRN\",\"url\":\"https://dev.integration-api.hmpps.service.justice.gov.uk/v1/persons/A123457\",\"lastModifiedDateTime\":\"2025-03-15T13:27:03.000000\"}","Timestamp":"2025-09-24T09:39:24.000Z","SignatureVersion":"1","Signature":"...","SigningCertURL":"...","UnsubscribeURL":"...","MessageAttributes":{"prisonId":{"Type":"String","Value":"TRN"},"eventType":{"Type":"String","Value":"PERSON_STATUS_CHANGED"}}}


helpFunction()
{
   echo ""
   echo "Usage: $0 -e environment -q queueName -f fileName"
   echo -e "\t-e Provide an environment"
   echo -e "\t-q Provide a queue name"
   echo -e "\t-f Provide a file name containing the events you want to push"
   exit 1 # Exit script after printing help
}

cleanUp(){
  # Clean up
  kubectl --namespace="$namespace" delete pod "$pod_name"
  exit 1 # Exit script after deleting pod
}

validateParams(){
  #Validation
  if [ -z "$env" ]
  then
      echo "environment not specified, please specify an environment: (dev/preprod/prod)";
      helpFunction
  fi

  if [ -z "$queueName" ]
  then
      echo "Queue not specified, please specify a queue name: e.g events_pnd_queue";
      helpFunction
  fi

  if [ -z "$fileName" ]
  then
      echo "No file containing events can be found";
      helpFunction
  fi

  if ! [ -f "$fileName" ]
  then
      echo "File $fileName cannot be found";
      helpFunction
  fi

  eventCount=$(grep -c '.'  "$fileName")

  if [ $eventCount -eq 0 ]
  then
      echo "File $fileName contains no events";
      helpFunction
  fi
}

startPod(){
  namespace="hmpps-integration-api-$env"
  pod_name="load-queue-messages-$env"
  queue_name="hmpps-integration-api-$env-$queueName"

  # Start service pod in background
  echo "Starting service pod '$pod_name'"
  kubectl run "$pod_name" \
    --namespace="$namespace" \
    --image=ghcr.io/ministryofjustice/hmpps-devops-tools:latest \
    --restart=Never \
    --overrides='{
      "spec": {
        "serviceAccount":"hmpps-integration-event",
        "containers": [
          {
            "name": "replay",
            "image": "ghcr.io/ministryofjustice/hmpps-devops-tools:latest",
            "command": ["sh", "-c", "sleep 600"],
            "resources": {
              "limits": {
                "cpu": "1000m",
                "memory": "1Gi"
              }
            }
          }
        ]
      }
    }' -- sh & sleep 5
  kubectl wait \
    --namespace="$namespace" \
    --for=condition=ready pod "$pod_name"
}

checkQueue(){
  # Get queue url
  queue_url=$(kubectl exec "$pod_name" --namespace="$namespace" -- aws sqs get-queue-url --queue-name "$queue_name" --query QueueUrl --output text)
  # Exit if the queue cannot be found
  if [ -z "$queue_url" ]
  then
      echo "Queue $queue_name can not be found in namespace $namespace. Make sure the specified queue name exists";
      cleanUp
  fi

  echo "Got queue URL for $queue_name: $queue_url"
}

load(){
  #  Copy files to service pod
  kubectl cp --namespace="$namespace" ./sqs-utils.py "$pod_name:/tmp/sqs-utils.py"
  kubectl cp --namespace="$namespace" $fileName "$pod_name:/tmp/messages.txt"

  # Load the messages
  kubectl exec "$pod_name" --namespace="$namespace" -- sh -c "python /tmp/sqs-utils.py send '$queue_url' < /tmp/messages.txt"
}

while getopts "e:q:f:" opt
do
   case "$opt" in
      e ) env="$OPTARG" ;;
      q ) queueName="$OPTARG" ;;
      f ) fileName="$OPTARG" ;;
      ? ) helpFunction ;; # Print helpFunction in case parameter is non-existent
   esac
done

validateParams
startPod
checkQueue

# Prompt user to check they want to continue
read -p "You are about to load $eventCount events to $queue_url. Are you sure you want to continue? <y/N> " prompt
if [[ $prompt == "y" || $prompt == "Y" || $prompt == "yes" || $prompt == "Yes" ]]
then
  load
else
  echo "Exiting without loading"
fi

cleanUp
