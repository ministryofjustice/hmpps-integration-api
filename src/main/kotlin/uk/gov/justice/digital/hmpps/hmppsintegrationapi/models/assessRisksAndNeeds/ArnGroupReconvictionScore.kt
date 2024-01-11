package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.GroupReconviction as HmppsGroupReconviction

data class ArnGroupReconvictionScore(
  val scoreLevel: String? = null,
) {
  fun toGroupReconviction(): HmppsGroupReconviction = HmppsGroupReconviction(
    scoreLevel = this.scoreLevel,
  )
}
