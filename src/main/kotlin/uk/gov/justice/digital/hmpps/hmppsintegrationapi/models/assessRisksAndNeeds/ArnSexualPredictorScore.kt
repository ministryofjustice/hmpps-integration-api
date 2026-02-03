package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SexualPredictor

data class ArnSexualPredictorScore(
  val ospIndecentScoreLevel: String? = null,
  val ospContactScoreLevel: String? = null,
  val ospIndirectImagePercentageScore: Int? = null,
  val ospDirectContactPercentageScore: Int? = null,
) {
  fun toSexualPredictor(useV2NumericalValue: Boolean = true): SexualPredictor =
    SexualPredictor(
      indecentScoreLevel = this.ospIndecentScoreLevel,
      contactScoreLevel = this.ospContactScoreLevel,
      indecentScore = if (useV2NumericalValue) this.ospIndirectImagePercentageScore else null,
      contactScore = if (useV2NumericalValue) this.ospDirectContactPercentageScore else null,
    )
}
