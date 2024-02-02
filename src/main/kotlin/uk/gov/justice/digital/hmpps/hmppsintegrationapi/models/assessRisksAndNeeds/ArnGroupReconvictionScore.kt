package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.GroupReconviction

data class ArnGroupReconvictionScore(
  val scoreLevel: String? = null,
) {
  fun toGroupReconviction(): GroupReconviction = GroupReconviction(
    scoreLevel = this.scoreLevel,
  )
}
