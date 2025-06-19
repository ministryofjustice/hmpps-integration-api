package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivityScheduleInstance

data class ActivitiesActivityScheduleInstance(
  val id: Long,
  val date: String,
  val startTime: String,
  val endTime: String,
  val timeSlot: String,
  val cancelled: Boolean,
  val cancelledTime: String?,
  val cancelledBy: String?,
  val attendances: List<ActivitiesAttendance>,
) {
  fun toActivityScheduleInstance() =
    ActivityScheduleInstance(
      scheduleInstanceId = this.id,
      date = this.date,
      startTime = this.startTime,
      endTime = this.endTime,
      timeSlot = this.timeSlot,
      cancelled = this.cancelled,
      cancelledTime = this.cancelledTime,
      attendances = this.attendances.map { it.toAttendance() },
    )
}
