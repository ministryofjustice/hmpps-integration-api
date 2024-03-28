package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskOfSeriousRecidivism

data class ArnRiskOfSeriousRecidivismScore(
  val scoreLevel: String? = null,
) {
  fun toRiskOfSeriousRecidivism(): RiskOfSeriousRecidivism =
    RiskOfSeriousRecidivism(
      scoreLevel = this.scoreLevel,
    )
}
