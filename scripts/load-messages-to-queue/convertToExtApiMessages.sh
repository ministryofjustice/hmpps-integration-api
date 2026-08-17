#!/bin/bash
##
## Converts the results of a CSV file exported from Application Insights
## into an integration event capable of being loaded into an hmpps-integration-api event queue
## e.g
##
##"2026-08-14T17:00:02.000000","PRISONER_BASE_LOCATION_CHANGED","v1/persons/A1234AB/prisoner-base-location",A1234AB,LPI
##"2026-08-14T17:00:04.000000","PERSON_EDUCATION_ASSESSMENTS_CHANGED","v1/persons/A123456/education/assessments",A123456,
##
## Usage:
## /convertToMessages.sh -e <ENV> -f <INPUT>
## e.g ./convertToMessages.sh -e dev -f sqlResults.txt > eventMessages.txt
## returns
## at `eventMessages.txt`
## {"Type":"Notification","MessageId":"...","TopicArn":"...","Message":"{\"eventId\":166,\"hmppsId\":\"A1234AB\",\"eventType\":\"PRISONER_BASE_LOCATION_CHANGED\",\"prisonId\":\"TRN\",\"url\":\"https://dev.integration-api.hmpps.service.justice.gov.uk/v1/persons/A1234AB/prisoner-base-location\",\"lastModifiedDateTime\":\"2017-03-15T15:47:51.000000\"}","Timestamp":"2025-09-24T09:39:24.000Z","SignatureVersion":"1","Signature":"...","SigningCertURL":"...","UnsubscribeURL":"...","MessageAttributes":{"prisonId":{"Type":"String","Value":"TRN"},"eventType":{"Type":"String","Value":"PRISONER_BASE_LOCATION_CHANGED"}}}
## {"Type":"Notification","MessageId":"...","TopicArn":"...","Message":"{\"eventId\":166,\"hmppsId\":\"A123456\",\"eventType\":\"PERSON_EDUCATION_ASSESSMENTS_CHANGED\",\"prisonId\":\"TRN\",\"url\":\"https://dev.integration-api.hmpps.service.justice.gov.uk/v1/persons/A123456/education/assessments\",\"lastModifiedDateTime\":\"2017-03-15T15:47:51.000000\"}","Timestamp":"2025-09-24T09:39:24.000Z","SignatureVersion":"1","Signature":"...","SigningCertURL":"...","UnsubscribeURL":"...","MessageAttributes":{"prisonId":{"Type":"String","Value":"TRN"},"eventType":{"Type":"String","Value":"PERSON_EDUCATION_ASSESSMENTS_CHANGED"}}}


helpFunction()
{
   echo ""
   echo "Usage: $0 -t eventType -e environment -f fileName"
   echo -e "\t-e Provide an environment"
   echo -e "\t-f Provide a file name containing the file you want to convert"
   exit 1 # Exit script after printing help
}

URL_SUFFIX=""
while getopts "t:e:f:u:" opt
do
   case "$opt" in
      e ) ENV="$OPTARG" ;;
      f ) INPUT="$OPTARG" ;;
      ? ) helpFunction ;; # Print helpFunction in case parameter is non-existent
   esac
done


if [ -z "$ENV" ]
then
    echo "environment not specified, please specify an environment: (dev/preprod/prod)";
    helpFunction
fi

if ! [[ "$ENV" =~ ^(dev|preprod|prod)$ ]]
then
    echo "Environment is not valid (dev/preprod/prod)";
    helpFunction
fi
if [[ "$ENV" == prod ]]
then
  URL_PREFIX=""
else
  URL_PREFIX="$ENV."
fi

if [ -z "$INPUT" ]
then
    echo "No file to convert has been specified";
    helpFunction
fi

if ! [ -f "$INPUT" ]
then
    echo "File $INPUT cannot be found";
    helpFunction
fi

while IFS="," read col1 col2 col3 col4 col5
do

  LAST_UPDATED=$(echo $col1 | sed 's/"//g' | sed 's/\xef\xbb\xbf//g')
  EVENT_TYPE=$(echo $col2 | sed 's/"//g')
  ENDPOINT=$(echo $col3 | sed 's/"//g')
  HMPPS_ID=$(echo $col4 | sed 's/"//g' | sed 's/\r//g')
  PRISON_ID=$(echo $col5 | sed 's/"//g' | sed 's/\r//g')
  TRN="TRN"
  EVENT_ID=166
  TIMESTAMP=$(date -u '+%Y-%m-%dT%H:%M:%S.000Z')
  date=$(echo $(($(date +'%s * 1000 + %-N / 1000000'))))
  URL="https://${URL_PREFIX}integration-api.hmpps.service.justice.gov.uk/$ENDPOINT"

  if [ -z "${PRISON_ID}" ]; then
    echo "{\"Type\":\"Notification\",\"MessageId\":\"...\",\"TopicArn\":\"...\",\"Message\":\"{\\\"eventId\\\":$EVENT_ID,\\\"hmppsId\\\":\\\"$HMPPS_ID\\\",\\\"eventType\\\":\\\"$EVENT_TYPE\\\",\\\"prisonId\\\":\\\"$TRN\\\",\\\"url\\\":\\\"$URL\\\",\\\"lastModifiedDateTime\\\":\\\"$LAST_UPDATED\\\"}\",\"Timestamp\":\"$TIMESTAMP\",\"SignatureVersion\":\"1\",\"Signature\":\"...\",\"SigningCertURL\":\"...\",\"UnsubscribeURL\":\"...\",\"MessageAttributes\":{\"prisonId\":{\"Type\":\"String\",\"Value\":\"$TRN\"},\"eventType\":{\"Type\":\"String\",\"Value\":\"$EVENT_TYPE\"}}}"
  else
    echo "{\"Type\":\"Notification\",\"MessageId\":\"...\",\"TopicArn\":\"...\",\"Message\":\"{\\\"eventId\\\":$EVENT_ID,\\\"hmppsId\\\":\\\"$HMPPS_ID\\\",\\\"eventType\\\":\\\"$EVENT_TYPE\\\",\\\"prisonId\\\":\\\"$PRISON_ID\\\",\\\"url\\\":\\\"$URL\\\",\\\"lastModifiedDateTime\\\":\\\"$LAST_UPDATED\\\"}\",\"Timestamp\":\"$TIMESTAMP\",\"SignatureVersion\":\"1\",\"Signature\":\"...\",\"SigningCertURL\":\"...\",\"UnsubscribeURL\":\"...\",\"MessageAttributes\":{\"prisonId\":{\"Type\":\"String\",\"Value\":\"$PRISON_ID\"},\"eventType\":{\"Type\":\"String\",\"Value\":\"$EVENT_TYPE\"}}}"
  fi

done < "$INPUT"
