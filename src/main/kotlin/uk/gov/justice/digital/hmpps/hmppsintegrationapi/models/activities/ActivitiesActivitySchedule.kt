package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

data class ActivitiesActivitySchedule(
  val id: Long,
  val description: String,
  val internalLocation: ActivitiesInternalLocation,
  val capacity: Int,
  val activity: ActivitiesActivity,
  val scheduleWeeks: Int,
  val slots: List<ActivitiesSlot>,
  val startDate: String,
  val endDate: String?,
  val usePrisonRegimeTime: Boolean,
)
