package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.policies

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.RedactionType.MASK
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.RedactionType.REMOVE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.redactionPolicy

val personLaoRedactions =
  listOf(
    Pair("$..gender", REMOVE),
    Pair("$..ethnicity", REMOVE),
    Pair("$..contactDetails", REMOVE),
  )

val laoRedactionPolicy =
  redactionPolicy(
    "lao-redactions",
  ) {
    responseRedactions {
      laoRejection {
        endpoints {
          -"/v1/persons/{hmppsId}/risk-management-plan"
          -"/v1/persons/{hmppsId}/risks/scores"
          -"/v1/persons/{hmppsId}/risks/serious-harm"
        }
      }
      jsonPath {
        laoOnly(true)
        endpoints {
          -"/v1/persons/{hmppsId}/licences/conditions"
        }
        redactions {
          -("$..condition" to MASK)
        }
      }
      jsonPath {
        laoOnly(true)
        endpoints {
          -"/v1/persons/{hmppsId}/risks/mappadetail"
          -"/v1/persons/{hmppsId}/risks/dynamic"
          -"/v1/persons/{hmppsId}/status-information"
        }
        redactions {
          -("$..notes" to MASK)
        }
      }
      jsonPath {
        laoOnly(true)
        endpoints {
          -"/v1/persons/{hmppsId}"
        }
        redactions {
          -personLaoRedactions
        }
      }
      jsonPath {
        laoOnly(true)
        endpoints {
          -"/v1/persons/{hmppsId}/alerts"
        }
        redactions {
          -("$..type" to REMOVE)
          -("$..typeDescription" to REMOVE)
          -("$..dateExpired" to REMOVE)
          -("$..expired" to REMOVE)
          -("$..active" to REMOVE)
        }
      }
      personSearchLao {
        redactions {
          -personLaoRedactions
        }
      }
      laoRejection {
        endpoints {
          -"/v1/persons/{hmppsId}/addresses"
        }
      }
    }
  }
