#!/bin/bash

###############################################################################################################
# Script to update the upstream API openapi specifications used to validate the test requests and responses   #
###############################################################################################################

updateOpenApiSpec() {
    from=$1
    to=$2
    {
      IFS= read -rd '' out
      IFS= read -rd '' http_code
      IFS= read -rd '' status
    } < <({ out=$(curl -sSL -o /dev/stderr -w "%{http_code}" "$from"); } 2>&1; printf '\0%s' "$out" "$?")


    echo  $out | yq eval -p yaml -o json '.'

    if [ "$http_code" != "200" ]; then
        echo "Call to $from returned $http_code"
    else
      echo $out | yq > $to
    fi
}

updateOpenApiSpec "https://prisoner-search.prison.service.justice.gov.uk/v3/api-docs"  "../src/test/resources/openapi-specs/prisoner-search.json"
updateOpenApiSpec "https://activities-api.prison.service.justice.gov.uk/v3/api-docs"  "../src/test/resources/openapi-specs/activities.json"
updateOpenApiSpec "https://health-and-medication-api.hmpps.service.justice.gov.uk/v3/api-docs"  "../src/test/resources/openapi-specs/health-and-medication.json"

## The learning and work progress spec seems to be in a yaml - this script will convert to JSON so that initial diff can be performed
## The application will happily accept yaml files, so no need to convert - this is just for comparison reasons
## Raised HIA-888 to look at specific issues with the updated openapi spec
## TODO updateOpenApiSpec "https://learningandworkprogress-api.hmpps.service.justice.gov.uk/openapi/EducationAndWorkPlanAPI.yml" "../src/test/resources/openapi-specs/plp.json"
## The location of the manage POM cases openapi spec is currently unknown see issue HIA-887
