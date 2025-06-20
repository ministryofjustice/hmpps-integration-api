package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PlannedDeallocation

data class ActivitiesPlannedDeallocation(
  val id: Long,
  val plannedDate: String,
  val plannedBy: String,
  val plannedReason: ActivitiesReason,
  val plannedAt: String,
) {
  fun toPlannedDeallocation() =
    PlannedDeallocation(
      plannedDate = this.plannedDate,
    )
}
