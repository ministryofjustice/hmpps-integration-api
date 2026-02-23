package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.GroupReconviction
import java.math.BigDecimal

data class ArnGroupReconvictionScore(
  val scoreLevel: String? = null,
  val twoYears: BigDecimal? = null,
) {
  fun toGroupReconviction(useV2NumericalValue: Boolean = true): GroupReconviction =
    GroupReconviction(
      scoreLevel = this.scoreLevel,
      score = if (useV2NumericalValue) this.twoYears else null,
    )
}
