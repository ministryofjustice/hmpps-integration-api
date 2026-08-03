package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.policies

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.RedactionType.MASK
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.RedactionType.REMOVE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.redactionPolicy

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
          -("$..forename" to MASK)
          -("$..surname" to MASK)
          -("$.data.communityOffenderManager.email" to MASK)
          -("$.data.communityOffenderManager.telephoneNumber" to MASK)
        }
      }
    }
  }
