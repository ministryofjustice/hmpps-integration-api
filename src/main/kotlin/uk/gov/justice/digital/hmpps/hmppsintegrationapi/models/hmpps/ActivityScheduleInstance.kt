package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class ActivityScheduleInstance(
  val scheduleInstanceId: Long,
  val date: String,
  val startTime: String,
  val endTime: String,
  val timeSlot: String,
  val cancelled: Boolean,
  val cancelledTime: String?,
  val cancelledBy: String?,
  val attendances: List<Attendance>,
)
