package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.policies.riskScores

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.RedactionType.REMOVE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.redactionPolicy

val epfRiskScoreRedactions =
  redactionPolicy(
    "epf-risk-score-redactions",
  ) {
    responseRedactions {
      jsonPath {
        endpoints {
          -"/v1/persons/{hmppsId}/risks/scores"
        }
        redactions {
          -("$..band" to REMOVE)
          -("$..scoreLevel" to REMOVE)
          -("$..indecentScoreLevel" to REMOVE)
          -("$..contactScoreLevel" to REMOVE)
        }
      }
    }
  }
