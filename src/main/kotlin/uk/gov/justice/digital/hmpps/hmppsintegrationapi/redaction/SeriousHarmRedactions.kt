package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType.MASK
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.redactionPolicy

val seriousHarmRedactions =
  redactionPolicy(
    "serious-harm-redactions",
  ) {
    responseRedactions {
      jsonPath {
        endpoints {
          -"/v1/persons/{hmppsId}/risks/serious-harm"
        }
        redactions {
          -("$..previousConcernsText" to MASK)
          -("$..currentConcernsText" to MASK)
          -("$..whoIsAtRisk" to MASK)
          -("$..natureOfRisk" to MASK)
          -("$..riskImminence" to MASK)
          -("$..riskIncreaseFactors" to MASK)
          -("$..riskMitigationFactors" to MASK)
        }
      }
    }
  }
