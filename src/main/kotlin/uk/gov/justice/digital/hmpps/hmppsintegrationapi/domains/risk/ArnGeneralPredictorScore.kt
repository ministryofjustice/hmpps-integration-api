package uk.gov.justice.digital.hmpps.hmppsintegrationapi.domains.risk

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.GeneralPredictor

data class ArnGeneralPredictorScore(
  val ogpRisk: String? = null,
) {
  fun toGeneralPredictor(): GeneralPredictor =
    GeneralPredictor(
      scoreLevel = this.ogpRisk,
    )
}
