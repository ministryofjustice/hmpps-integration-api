package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

data class ActivitiesPlannedDeallocation(
  val id: Long,
  val plannedDate: String,
  val plannedBy: String,
  val plannedReason: ActivitiesReason,
  val plannedAt: String,
)
