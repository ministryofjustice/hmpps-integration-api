package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.managePOMCase

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonOffenderManager

data class AllocationPrimaryPOM(
  val manager: AllocationManager = AllocationManager(),
  val prison: AllocationPrison = AllocationPrison(),
) {
  fun toPrisonOffenderManager(): PrisonOffenderManager =
    PrisonOffenderManager(
      forename = this.manager.forename,
      surname = this.manager.surname,
    )
}
