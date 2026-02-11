package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.riskScores

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType.REMOVE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.redactionPolicy

val generalRiskScoreRedactions =
  redactionPolicy(
    "general-risk-score-redactions",
  ) {
    responseRedactions {
      jsonPath {
        endpoints {
          -"/v1/persons/.*/risks/scores"
        }
        redactions {
          -("$..score" to REMOVE)
          -("$..indecentScore" to REMOVE)
          -("$..contactScore" to REMOVE)
        }
      }
    }
  }
