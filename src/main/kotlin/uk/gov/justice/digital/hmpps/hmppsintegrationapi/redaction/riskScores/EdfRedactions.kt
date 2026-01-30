package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.riskScores

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig.RedactionType.REMOVE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.redactionPolicy

val edfRiskScoreRedactions =
  redactionPolicy(
    "edf-risk-score-redactions",
  ) {
    responseRedactions {
      jsonPath {
        endpoints {
          -"/v1/persons/.*/risks/scores"
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
