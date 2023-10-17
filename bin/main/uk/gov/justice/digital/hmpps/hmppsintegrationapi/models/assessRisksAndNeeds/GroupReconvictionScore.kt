package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.GroupReconviction as IntegrationAPIGroupReconviction

data class GroupReconvictionScore(
  val scoreLevel: String? = null,
) {
  fun toGroupReconviction(): IntegrationAPIGroupReconviction = IntegrationAPIGroupReconviction(
    scoreLevel = this.scoreLevel,
  )
}
