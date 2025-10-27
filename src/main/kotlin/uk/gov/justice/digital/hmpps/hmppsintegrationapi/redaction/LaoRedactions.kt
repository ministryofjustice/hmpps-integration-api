package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType.MASK
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.redactionPolicy

val laoRedactionPolicy =
  redactionPolicy(
    "lao-redactions",
    laoOnly = true,
    endpoints =
      listOf(
        "/v1/persons/.*/licences/conditions",
        "/v1/persons/.*/risks/dynamic",
        "/v1/persons/.*/risks/mappadetail",
        "/v1/persons/.*/status-information",
      ),
  ) {
    responseRedactions {
      jsonPath {
        includes {
          path("$..notes", MASK)
          path("$..condition", MASK)
        }
      }
    }
  }
