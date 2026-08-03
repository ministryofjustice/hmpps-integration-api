package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.policies.riskScores

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.RedactionType.REMOVE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.redactionPolicy

val generalRiskScoreRedactions =
  redactionPolicy(
    "general-risk-score-redactions",
  ) {
    responseRedactions {
      jsonPath {
        endpoints {
          -"/v1/persons/{hmppsId}/risks/scores"
        }
        redactions {
          -("$..score" to REMOVE)
          -("$..indecentScore" to REMOVE)
          -("$..contactScore" to REMOVE)
        }
      }
    }
  }
