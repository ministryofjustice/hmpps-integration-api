#!/bin/bash

helpFunction()
{
   echo ""
   echo "Usage: $0 -e endpoint -i hmppsId"
   echo -e "\t-e Provide an endpoint suffix E.g. hmpps_prod_exec.sh -e \"images\""
   echo -e "\t-i (optional) provide an hmppsId, in the case one isn't provided a default one for a Anna Franey will be used"
   exit 1 # Exit script after printing help
}

while getopts "e:i:" opt
do
   case "$opt" in
      e ) endpoint="$OPTARG" ;;
      i ) suppliedId="$OPTARG" ;;
      ? ) helpFunction ;; # Print helpFunction in case parameter is non-existent
   esac
done

#helpFunction in case parameters are empty
if [ -z "$endpoint" ]
then
    echo "endpoint not specified, please specify an endpoint in the form: (endpoint1 endpoint2)";
    echo "List of endpoints: persons | personId | images | imageData | addresses | offences | alerts | sentences | sentenceDates | riskScores | needs | risks | personDetails"
    helpFunction
fi

# Begin script in case all parameters are correct

################
# Call our API #
################
#
APIKEY="x-api-key: <insert your api key here>"
FIRST_NAME=ANNA #Change this is using persons endpoint
SURNAME=FRANEY #Change this is using persons endpoint
IMAGE_ID=""

if [ -z $suppliedId ]
then
    HMPPS_ID="1974%2F4990457K" # Anna Franey example
    echo "HMPPS_ID = $suppliedId"
else
    HMPPS_ID=$suppliedId
    echo "Supplied ID = $suppliedId"
fi

case $endpoint in
    "persons")
    #person endpoint
        curl -H "${APIKEY}" "https://dev.integration-api.hmpps.service.justice.gov.uk/v1/persons?first_name=$FIRST_NAME&last_name=$SURNAME" --cert ~/bin/client.pem --key ~/bin/client.key | jq
        ;;
    "personId")
    #persons/{hmppsId} endpoint
        curl -H "${APIKEY}" https://dev.integration-api.hmpps.service.justice.gov.uk/v1/persons/$HMPPS_ID --cert ~/bin/client.pem --key ~/bin/client.key | jq #pncID hmppsID
        ;;
    "images")
    #persons/{hmppsId}/images
        curl -H "${APIKEY}" https://dev.integration-api.hmpps.service.justice.gov.uk/v1/persons/$HMPPS_ID/images --cert ~/bin/client.pem --key ~/bin/client.key | jq #Returns error for nothing
        ;;
    "imageData")
    #images/{id}
        curl -H "${APIKEY}" https://dev.integration-api.hmpps.service.justice.gov.uk/v1/images/$IMAGE_ID --cert ~/bin/client.pem --key ~/bin/client.key | jq
        ;;
    "addresses")
    #persons/{hmppsId}/addresses
        curl -H "${APIKEY}" https://dev.integration-api.hmpps.service.justice.gov.uk/v1/persons/$HMPPS_ID/addresses --cert ~/bin/client.pem --key ~/bin/client.key | jq #Returns empty with pagination
        ;;
    "offences")
    #persons/{hmppsId}/offences
        curl -H "${APIKEY}" https://dev.integration-api.hmpps.service.justice.gov.uk/v1/persons/$HMPPS_ID/offences --cert ~/bin/client.pem --key ~/bin/client.key | jq # Returns empty with pagination
        ;;
    "alerts")
    #persons/{hmppsId}/alerts
        curl -H "${APIKEY}" https://dev.integration-api.hmpps.service.justice.gov.uk/v1/persons/$HMPPS_ID/alerts --cert ~/bin/client.pem --key ~/bin/client.key | jq # Returns empty with pagination
        ;;
    "sentences")
    #persons/{hmppsId}/sentences
        curl -H "${APIKEY}" https://dev.integration-api.hmpps.service.justice.gov.uk/v1/persons/$HMPPS_ID/sentences --cert ~/bin/client.pem --key ~/bin/client.key | jq # Returns empty with pagination
        ;;
    "sentenceDates")
    #persons/{hmppsId}/sentences/latest-key-dates-and-adjustments
        curl -H "${APIKEY}" https://dev.integration-api.hmpps.service.justice.gov.uk/v1/persons/$HMPPS_ID/sentences/latest-key-dates-and-adjustments --cert ~/bin/client.pem --key ~/bin/client.key | jq # Returns null
        ;;
    "riskScores")
    #persons/{hmppsId}/risks/scores
        curl -H "${APIKEY}" https://dev.integration-api.hmpps.service.justice.gov.uk/v1/persons/$HMPPS_ID/risks/scores --cert ~/bin/client.pem --key ~/bin/client.key | jq # Returns 404
        ;;
    "needs")
    #persons/{hmppsId}/needs
        curl -H "${APIKEY}" https://dev.integration-api.hmpps.service.justice.gov.uk/v1/persons/$HMPPS_ID/needs --cert ~/bin/client.pem --key ~/bin/client.key | jq # Returns 404
        ;;
    "risks")
    #persons/{hmppsId}/risks
        curl -H "${APIKEY}" https://dev.integration-api.hmpps.service.justice.gov.uk/v1/persons/$HMPPS_ID/risks --cert ~/bin/client.pem --key ~/bin/client.key | jq # Returns 404
        ;;
    "personDetails")
    #/epf/person-details/{hmppsId}/{eventNumber}
        curl -H "${APIKEY}" https://dev.integration-api.hmpps.service.justice.gov.uk/v1/epf/person-details/$HMPPS_ID/1 --cert ~/bin/client.pem --key ~/bin/client.key | jq # Returns 404
        ;;
esac