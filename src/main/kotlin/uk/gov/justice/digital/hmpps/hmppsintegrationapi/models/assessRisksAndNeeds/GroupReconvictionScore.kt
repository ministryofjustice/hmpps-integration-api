package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.GroupReconviction as HmppsGroupReconviction

data class GroupReconvictionScore(
  val scoreLevel: String? = null,
) {
  fun toGroupReconviction(): HmppsGroupReconviction = HmppsGroupReconviction(
    scoreLevel = this.scoreLevel,
  )
}
