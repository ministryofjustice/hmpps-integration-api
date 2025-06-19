package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class PlannedSuspension(
  val plannedStartDate: String,
  val plannedEndDate: String?,
  val paid: Boolean,
)
