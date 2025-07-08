package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivityScheduledInstanceForPrisoner

data class ActivitiesActivityScheduledInstanceForPrisoner(
  val id: Long,
  val date: String,
  val startTime: String,
  val endTime: String,
  val timeSlot: String,
  val cancelled: Boolean,
  val cancelledTime: String?,
  val cancelledBy: String?,
  val cancelledReason: String? = null,
  val cancelledIssuePayment: Boolean? = null,
  val comment: String? = null,
  val previousScheduledInstanceId: Long? = null,
  val previousScheduledInstanceDate: String? = null,
  val nextScheduledInstanceId: Long? = null,
  val nextScheduledInstanceDate: String? = null,
  val attendances: List<ActivitiesAttendance>,
  val advanceAttendances: List<ActivitiesAdvanceAttendance>,
  val activitySchedule: ActivitiesActivitySchedule,
) {
  fun toActivityScheduledInstanceForPrisoner() =
    ActivityScheduledInstanceForPrisoner(
      id = this.id,
      date = this.date,
      startTime = this.startTime,
      endTime = this.endTime,
      timeSlot = this.timeSlot,
      cancelled = this.cancelled,
      cancelledTime = this.cancelledTime,
      cancelledBy = this.cancelledBy,
      cancelledReason = this.cancelledReason,
      cancelledIssuePayment = this.cancelledIssuePayment,
      comment = this.comment,
      previousScheduledInstanceId = this.previousScheduledInstanceId,
      previousScheduledInstanceDate = this.previousScheduledInstanceDate,
      nextScheduledInstanceId = this.nextScheduledInstanceId,
      nextScheduledInstanceDate = this.nextScheduledInstanceDate,
      attendances = this.attendances.map { it.toAttendance() },
      advanceAttendances = this.advanceAttendances.map { it.toAdvanceAttendance() },
      activitySchedule = this.activitySchedule.toActivitySchedule(),
    )
}
