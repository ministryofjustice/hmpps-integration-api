package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

data class ActivitiesActivityScheduleInstance(
  val id: Long,
  val date: String,
  val startTime: String,
  val endTime: String,
  val timeSlot: String,
  val cancelled: Boolean,
  val cancelledTime: String?,
  val cancelledBy: String?,
  val attendances: List<ActivitiesActivityAttendance>,
)
