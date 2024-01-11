package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.GeneralPredictor as HmppsGeneralPredictor

data class GeneralPredictorScore(
  val ogpRisk: String? = null,
) {
  fun toGeneralPredictor(): HmppsGeneralPredictor = HmppsGeneralPredictor(
    scoreLevel = this.ogpRisk,
  )
}
