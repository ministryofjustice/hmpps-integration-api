package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

class ActivitiesActivitySchedule(
  val id: Long,
  val description: String,
  val internalLocation: String?,
  val capacity: Int,
  val activity: ActivitiesActivity,
  val scheduleWeeks: Int,
  val slots: List<ActivitiesSlot>,
  val startDate: String,
  val endDate: String?,
  val usePrisonRegimeTime: Boolean,
)
