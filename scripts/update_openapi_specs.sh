#!/bin/bash

###############################################################################################################
# Script to update the upstream API openapi specifications used to validate the test requests and responses   #
###############################################################################################################

updateOpenApiSpec() {
  from=$1
  to=$2

  echo $from
  curl -k $from > $to
}

updateOpenApiSpec "https://prisoner-search-dev.prison.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/prisoner-search.json"
updateOpenApiSpec "https://activities-api-dev.prison.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/activities.json"
updateOpenApiSpec "https://health-and-medication-api-dev.hmpps.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/health-and-medication.json"
updateOpenApiSpec "https://assess-risks-and-needs-dev.hmpps.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/assess-risks-and-needs.json"
updateOpenApiSpec "https://hmpps-person-record-dev.hmpps.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/core-person-record.json"
updateOpenApiSpec "https://manage-adjudications-api-dev.hmpps.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/manage-adjudications.json"
updateOpenApiSpec "https://dev.moic.service.justice.gov.uk/v3/api-docs.json" "../src/test/resources/openapi-specs/manage-POM.json"
updateOpenApiSpec "https://external-api-and-delius-dev.hmpps.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/ndelius.json"
updateOpenApiSpec "https://learningandworkprogress-api-dev.hmpps.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/plp.json"
updateOpenApiSpec "https://prisoner-base-location-api-dev.hmpps.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/prisoner-base-location.json"
updateOpenApiSpec "https://probation-offender-search-dev.hmpps.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/probation-offender-search.json"
updateOpenApiSpec "https://manage-users-api-dev.hmpps.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/manage-users.json"
updateOpenApiSpec "https://court-register-api-dev.hmpps.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/court_register.json"
updateOpenApiSpec "https://remand-and-sentencing-api-dev.hmpps.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/remand-and-sentencing.json"
updateOpenApiSpec "https://locations-inside-prison-api-dev.hmpps.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/locations-inside-prison.json"
updateOpenApiSpec "https://alerts-api-dev.hmpps.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/prisoner-alerts.json"
updateOpenApiSpec "https://incentives-api-dev.hmpps.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/incentives.json"
updateOpenApiSpec "https://hmpps-manage-prison-visits-orchestration-dev.prison.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/manage-prisons.json"
updateOpenApiSpec "https://personal-relationships-api-dev.hmpps.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/relationships.json"
updateOpenApiSpec "https://non-associations-api-dev.hmpps.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/non-associations.json"
updateOpenApiSpec "https://prison-api-dev.prison.service.justice.gov.uk/v3/api-docs" "../src/test/resources/openapi-specs/prison-api.json"
