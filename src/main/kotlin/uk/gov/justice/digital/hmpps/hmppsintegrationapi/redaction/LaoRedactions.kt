package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType.MASK
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType.REMOVE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.redactionPolicy

val laoRedactionPolicy =
  redactionPolicy(
    "lao-redactions",
  ) {
    responseRedactions {
      laoRejection {
        paths {
          -"/v1/persons/.*/risk-management-plan"
          -"/v1/persons/.*/risks/scores"
          -"/v1/persons/.*/risks/serious-harm"
        }
      }
      jsonPath {
        laoOnly(true)
        paths {
          -"/v1/persons/.*/licences/conditions"
        }
        includes {
          path("$..condition", MASK)
        }
      }
      jsonPath {
        laoOnly(true)
        paths {
          -"/v1/persons/.*/risks/mappadetail"
          -"/v1/persons/.*/risks/dynamic"
          -"/v1/persons/.*/status-information"
        }
        includes {
          path("$..notes", MASK)
        }
      }
      jsonPath {
        laoOnly(true)
        paths {
          -"/v1/persons/[^/]*$"
        }
        includes {
          path("$..middleName", MASK)
          path("$..gender", MASK)
          path("$..ethnicity", MASK)
          path("$..contactDetails", REMOVE)
          path("$..aliases", REMOVE)
        }
      }
      laoRejection {
        paths {
          -"/v1/persons/.*/addresses"
        }
      }
    }
  }
