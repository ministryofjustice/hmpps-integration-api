package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.GeneralPredictor
import java.math.BigDecimal

data class ArnGeneralPredictorScore(
  val ogpRisk: String? = null,
  val ogp2Year: BigDecimal? = null,
) {
  fun toGeneralPredictor(useV2NumericalValue: Boolean = true): GeneralPredictor =
    GeneralPredictor(
      scoreLevel = this.ogpRisk,
      score = if (useV2NumericalValue) this.ogp2Year else null,
    )
}
