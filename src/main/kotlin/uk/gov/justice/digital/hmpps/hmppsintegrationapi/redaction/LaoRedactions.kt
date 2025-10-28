package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType.MASK
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.redactionPolicy

val laoRedactionPolicy =
  redactionPolicy(
    "lao-redactions",
  ) {
    responseRedactions {
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
    }
  }
