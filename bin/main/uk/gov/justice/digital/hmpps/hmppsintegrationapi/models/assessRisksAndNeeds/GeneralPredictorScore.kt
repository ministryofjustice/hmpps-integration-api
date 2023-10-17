package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.GeneralPredictor as IntegrationAPIGeneralPredictor

data class GeneralPredictorScore(
  val ogpRisk: String? = null,
) {
  fun toGeneralPredictor(): IntegrationAPIGeneralPredictor = IntegrationAPIGeneralPredictor(
    scoreLevel = this.ogpRisk,
  )
}
