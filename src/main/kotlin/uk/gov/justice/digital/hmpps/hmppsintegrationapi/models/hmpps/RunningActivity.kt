package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class RunningActivity(
  val id: Long,
  val activityName: String?,
  val category: ActivityCategory,
  val capacity: Int,
  val allocated: Int,
  val waitlisted: Int,
  val activityState: String,
)
