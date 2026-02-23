package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ViolencePredictor
import java.math.BigDecimal

data class ArnViolencePredictorScore(
  val ovpRisk: String? = null,
  val twoYears: BigDecimal? = null,
) {
  fun toViolencePredictor(useV2NumericalValue: Boolean = true): ViolencePredictor =
    ViolencePredictor(
      scoreLevel = this.ovpRisk,
      score = if (useV2NumericalValue) this.twoYears else null,
    )
}
