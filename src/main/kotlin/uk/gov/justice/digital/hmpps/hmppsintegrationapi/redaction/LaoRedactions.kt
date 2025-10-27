package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType.MASK
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.redactionPolicy

val laoRedactionPolicy =
  redactionPolicy(
    "lao-redactions",
    laoOnly = true,
  ) {
    responseRedactions {
      jsonPath {
        paths {
          -"/v1/persons/.*/licences/conditions"
        }
        includes {
          path("$..condition", MASK)
        }
      }
      jsonPath {
        paths {
          -"/v1/persons/.*/risks/dynamic"
        }
        includes {
          path("$..notes", MASK)
        }
      }
      jsonPath {
        paths {
          -"/v1/persons/.*/risks/mappadetail"
        }
        includes {
          path("$..notes", MASK)
        }
      }
      jsonPath {
        paths {
          -"/v1/persons/.*/status-information"
        }
        includes {
          path("$..notes", MASK)
        }
      }
    }
  }
