package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType.MASK
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType.REMOVE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.redactionPolicy

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
          -"/v1/persons/.*/risk-management-plan"
          -"/v1/persons/.*/risks/scores"
          -"/v1/persons/.*/risks/serious-harm"
        }
      }
      jsonPath {
        laoOnly(true)
        endpoints {
          -"/v1/persons/.*/licences/conditions"
        }
        redactions {
          -("$..condition" to MASK)
        }
      }
      jsonPath {
        laoOnly(true)
        endpoints {
          -"/v1/persons/.*/risks/mappadetail"
          -"/v1/persons/.*/risks/dynamic"
          -"/v1/persons/.*/status-information"
        }
        redactions {
          -("$..notes" to MASK)
        }
      }
      jsonPath {
        laoOnly(true)
        endpoints {
          -"/v1/persons/[^/]*$"
        }
        redactions {
          -personLaoRedactions
        }
      }
      jsonPath {
        laoOnly(true)
        endpoints {
          -"/v1/persons/.*/alerts"
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
          -"/v1/persons/.*/addresses"
          -"/v1/persons/.*/sentences"
        }
      }
    }
  }
