package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SexualPredictor
import java.math.BigDecimal

data class ArnSexualPredictorScore(
  val ospIndecentScoreLevel: String? = null,
  val ospContactScoreLevel: String? = null,
  val ospIndirectImagePercentageScore: BigDecimal? = null,
  val ospDirectContactPercentageScore: BigDecimal? = null,
) {
  fun toSexualPredictor(
    useV2NumericalValue: Boolean = true,
    sendDecimals: Boolean = true,
  ): SexualPredictor =
    SexualPredictor(
      indecentScoreLevel = this.ospIndecentScoreLevel,
      contactScoreLevel = this.ospContactScoreLevel,
      indecentScore = if (useV2NumericalValue) roundDownIfRequired(this.ospIndirectImagePercentageScore, sendDecimals) else null,
      contactScore = if (useV2NumericalValue) roundDownIfRequired(this.ospDirectContactPercentageScore, sendDecimals) else null,
    )
}
