package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskOfSeriousRecidivism as HmppsRiskOfSeriousRecidivism

data class RiskOfSeriousRecidivismScore(
  val scoreLevel: String? = null,
) {
  fun toRiskOfSeriousRecidivism(): HmppsRiskOfSeriousRecidivism = HmppsRiskOfSeriousRecidivism(
    scoreLevel = this.scoreLevel,
  )
}
