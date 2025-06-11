package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

data class ActivitiesRunningActivity(
  val id: Long,
  val activityName: String?,
  val category: ActivitiesActivityCategory,
  val capacity: Int,
  val allocated: Int,
  val waitlisted: Int,
  val createdTime: String,
  val activityState: String,
)
