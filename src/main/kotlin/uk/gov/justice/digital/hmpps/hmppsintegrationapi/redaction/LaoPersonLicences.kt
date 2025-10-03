package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType.MASK
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.redactionPolicy

val laoPersonLicencesRedactionPolicy =
  redactionPolicy("lao-person-licences") {
    responseRedactions {
      redaction {
        type(MASK)
        paths {
          -"/v1/persons/.*/licences/conditions"
        }
        includes {
          -"$..conditions[*].condition"
        }
      }
    }
  }
