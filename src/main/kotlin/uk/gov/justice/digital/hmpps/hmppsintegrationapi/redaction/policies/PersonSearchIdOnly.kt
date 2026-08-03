package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.policies

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.RedactionType.REMOVE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.redactionPolicy

val personSearchIdOnly =
  redactionPolicy(
    "person-search-id-only",
  ) {
    responseRedactions {
      jsonPath {
        endpoints {
          -"/v1/persons"
        }
        redactions {
          -("$..firstName" to REMOVE)
          -("$..lastName" to REMOVE)
          -("$..middleName" to REMOVE)
          -("$..dateOfBirth" to REMOVE)
          -("$..gender" to REMOVE)
          -("$..ethnicity" to REMOVE)
          -("$..aliases" to REMOVE)
          -("$..contactDetails" to REMOVE)
          -("$..currentRestriction" to REMOVE)
          -("$..restrictionMessage" to REMOVE)
          -("$..currentExclusion" to REMOVE)
          -("$..exclusionMessage" to REMOVE)
        }
      }
    }
  }
