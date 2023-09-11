package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.GeneralPredictorScore as IntegrationAPIGeneralPredictorScore

data class GeneralPredictorScore(
  val ogpTotalWeightedScore: Int? = null,
) {
  fun toGeneralPredictorScore(): IntegrationAPIGeneralPredictorScore = IntegrationAPIGeneralPredictorScore(
    totalWeightedScore = this.ogpTotalWeightedScore,
  )
}
