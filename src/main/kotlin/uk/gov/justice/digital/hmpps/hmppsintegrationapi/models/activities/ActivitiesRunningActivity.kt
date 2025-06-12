package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RunningActivity

data class ActivitiesRunningActivity(
  val id: Long,
  val activityName: String?,
  val category: ActivitiesActivityCategory,
  val capacity: Int,
  val allocated: Int,
  val waitlisted: Int,
  val createdTime: String,
  val activityState: String,
) {
  fun toRunningActivity() =
    RunningActivity(
      id = this.id,
      activityName = this.activityName,
      category = this.category.toActivityCategory(),
      capacity = this.capacity,
      allocated = this.allocated,
      waitlisted = this.waitlisted,
      activityState = this.activityState,
    )
}
