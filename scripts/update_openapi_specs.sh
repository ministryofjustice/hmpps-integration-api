#!/bin/bash

###############################################################################################################
# Script to update the upstream API openapi specifications used to validate the test requests and responses   #
###############################################################################################################

updateOpenApiSpec() {
  from=$1
  to=$2

  echo $from
  curl -k $from | jq . > $to

#    echo  $out | yq eval -p yaml -o json '.'
#
#    if [ "$http_code" != "200" ]; then
#        echo "Call to $from returned $http_code"
#    else
#      echo $out | yq > $to
#    fi
}

updateOpenApiSpec "https://activities-api-dev.prison.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/activities.json"
updateOpenApiSpec "https://assess-risks-and-needs.hmpps.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/assess-risks-and-needs.json"
updateOpenApiSpec "https://hmpps-person-record.hmpps.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/core-person-record.json"
updateOpenApiSpec "https://health-and-medication-api-dev.hmpps.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/health-and-medication.json"
updateOpenApiSpec "https://manage-adjudications-api-dev.hmpps.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/manage-adjudications.json"
updateOpenApiSpec "https://dev.moic.service.justice.gov.uk/v3/api-docs.json" "../src/test/resources/openapi-specs/manage-POM.json"
updateOpenApiSpec "https://external-api-and-delius-dev.hmpps.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/ndelius.json"
updateOpenApiSpec "https://learningandworkprogress-api-dev.hmpps.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/plp.json"
updateOpenApiSpec "https://prisoner-base-location-api.hmpps.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/prisoner-base-location.json"
updateOpenApiSpec "https://prisoner-search-dev.prison.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/prisoner-search.json"


## The learning and work progress spec seems to be in a yaml - this script will convert to JSON so that initial diff can be performed
## The application will happily accept yaml files, so no need to convert - this is just for comparison reasons
## Raised HIA-888 to look at specific issues with the updated openapi spec
## TODO updateOpenApiSpec "https://learningandworkprogress-api.hmpps.service.justice.gov.uk/openapi/EducationAndWorkPlanAPI.yml" "../src/test/resources/openapi-specs/plp.json"
## The location of the manage POM cases openapi spec is currently unknown see issue HIA-887
