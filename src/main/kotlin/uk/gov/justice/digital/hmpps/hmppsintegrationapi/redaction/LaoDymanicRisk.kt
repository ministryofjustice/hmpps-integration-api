package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType.MASK
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.redactionPolicy

val laoDynamicRiskRedactionPolicy =
  redactionPolicy("lao-dynamic-risk") {
    responseRedactions {
      redaction {
        type(MASK)
        paths {
          -"/v1/persons/.*/risks/mappadetail"
        }
        includes {
          -"$..notes"
        }
      }
    }
  }
