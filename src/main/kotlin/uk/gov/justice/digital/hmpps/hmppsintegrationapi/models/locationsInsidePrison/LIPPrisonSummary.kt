package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Capacity

data class LIPPrisonSummary(
  val prisonName: String,
  val workingCapacity: Int,
  val signedOperationalCapacity: Int,
  val maxCapacity: Int,
  val numberOfCellLocations: Int,
) {
  fun toCapacity(): Capacity =
    Capacity(
      maxCapacity = this.maxCapacity,
      signedCapacity = this.signedOperationalCapacity,
      workingCapacity = this.workingCapacity,
    )
}
