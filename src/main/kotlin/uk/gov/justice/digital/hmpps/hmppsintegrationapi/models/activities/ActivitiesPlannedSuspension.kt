package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

data class ActivitiesPlannedSuspension(
  val plannedStartDate: String,
  val plannedEndDate: String?,
  val caseNoteId: Long?,
  val plannedBy: String,
  val plannedAt: String,
  val paid: Boolean,
)
