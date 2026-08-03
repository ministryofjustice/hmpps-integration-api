package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.policies

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType.MASK
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.redactionPolicy

val risksTextRedactions =
  redactionPolicy(
    "risks-text-redactions",
  ) {
    responseRedactions {
      jsonPath {
        endpoints {
          -"/v1/persons/{hmppsId}/risks/serious-harm"
          -"/v1/persons/{hmppsId}/risks/dynamic"
        }
        redactions {
          -("$..previousConcernsText" to MASK)
          -("$..currentConcernsText" to MASK)
          -("$..whoIsAtRisk" to MASK)
          -("$..natureOfRisk" to MASK)
          -("$..riskImminence" to MASK)
          -("$..riskIncreaseFactors" to MASK)
          -("$..riskMitigationFactors" to MASK)
          -("$..notes" to MASK)
        }
      }
    }
  }
