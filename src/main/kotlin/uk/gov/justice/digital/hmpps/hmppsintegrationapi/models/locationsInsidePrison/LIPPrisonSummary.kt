package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison

data class LIPPrisonSummary(
  val prisonName: String,
  val workingCapacity: Int,
  val signedOperationalCapacity: Int,
  val maxCapacity: Int,
  val numberOfCellLocations: Int,
)
