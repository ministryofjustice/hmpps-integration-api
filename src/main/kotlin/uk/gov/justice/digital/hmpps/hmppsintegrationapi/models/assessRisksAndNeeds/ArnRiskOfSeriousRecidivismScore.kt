package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskOfSeriousRecidivism
import java.math.BigDecimal

data class ArnRiskOfSeriousRecidivismScore(
  val scoreLevel: String? = null,
  val percentageScore: BigDecimal? = null,
) {
  fun toRiskOfSeriousRecidivism(useV2NumericalValue: Boolean = true): RiskOfSeriousRecidivism =
    RiskOfSeriousRecidivism(
      scoreLevel = this.scoreLevel,
      score = if (useV2NumericalValue) this.percentageScore else null,
    )
}
