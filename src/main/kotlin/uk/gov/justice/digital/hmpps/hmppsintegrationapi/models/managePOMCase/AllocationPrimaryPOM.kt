package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.managePOMCase

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Prison
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonOfficerManager

data class AllocationPrimaryPOM(
  val manager: AllocationManager = AllocationManager(),
  val prison: AllocationPrison = AllocationPrison(),
) {
  fun toPrisonOfficerManager(): PrisonOfficerManager =
    PrisonOfficerManager(
      forename = this.manager.forename,
      surname = this.manager.surname,
      prison =
        Prison(
          code = this.prison.code,
        ),
    )
}
