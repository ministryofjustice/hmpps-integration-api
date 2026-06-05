package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType.REMOVE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.redactionPolicy

val personResponsibleOfficerRedactions =
  redactionPolicy(
    "person-responsible-officer-redactions",
  ) {
    responseRedactions {
      jsonPath {
        endpoints {
          -"/v1/persons/{hmppsId}/person-responsible-officer"
        }
        redactions {
          -("$..prisonOffenderManager" to REMOVE)
        }
      }
    }
  }
