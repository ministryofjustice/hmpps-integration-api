package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PlannedSuspension

data class ActivitiesPlannedSuspension(
  val plannedStartDate: String,
  val plannedEndDate: String?,
  val caseNoteId: Long?,
  val plannedBy: String,
  val plannedAt: String,
  val paid: Boolean,
) {
  fun toPlannedSuspension() =
    PlannedSuspension(
      plannedStartDate = this.plannedStartDate,
      plannedEndDate = this.plannedEndDate,
      paid = this.paid,
    )
}
