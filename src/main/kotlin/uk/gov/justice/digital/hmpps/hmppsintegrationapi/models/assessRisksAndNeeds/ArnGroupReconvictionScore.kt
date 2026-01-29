package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.GroupReconviction

data class ArnGroupReconvictionScore(
  val scoreLevel: String? = null,
  val twoYears: Int? = null,
) {
  fun toGroupReconviction(useV2NumericalValue: Boolean = true): GroupReconviction =
    GroupReconviction(
      scoreLevel = this.scoreLevel,
      twoYears = if (useV2NumericalValue) this.twoYears else null,
    )
}
