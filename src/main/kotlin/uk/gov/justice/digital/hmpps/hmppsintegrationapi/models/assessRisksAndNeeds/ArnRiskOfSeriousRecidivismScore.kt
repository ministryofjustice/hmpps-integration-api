package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskOfSeriousRecidivism

data class ArnRiskOfSeriousRecidivismScore(
  val scoreLevel: String? = null,
  val percentageScore: Int? = null,
) {
  fun toRiskOfSeriousRecidivism(useV2NumericalValue: Boolean = true): RiskOfSeriousRecidivism =
    RiskOfSeriousRecidivism(
      scoreLevel = this.scoreLevel,
      score = if (useV2NumericalValue) this.percentageScore else null,
    )
}
