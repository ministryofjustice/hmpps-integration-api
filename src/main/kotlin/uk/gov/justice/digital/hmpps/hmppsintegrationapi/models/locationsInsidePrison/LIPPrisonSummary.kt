package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonCapacity

data class LIPPrisonSummary(
  val prisonName: String,
  val workingCapacity: Int,
  val signedOperationalCapacity: Int,
  val maxCapacity: Int,
  val numberOfCellLocations: Int,
) {
  fun toPrisonCapacity(): PrisonCapacity =
    PrisonCapacity(
      maxCapacity = this.maxCapacity,
      signedCapacity = this.signedOperationalCapacity,
      workingCapacity = this.workingCapacity,
    )
}
