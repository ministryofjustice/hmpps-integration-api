package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivityScheduledInstanceForPrisoner
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class ActivitiesActivityScheduledInstanceForPrisoner(
  val scheduledInstanceId: Long,
  val allocationId: Long,
  val prisonCode: String,
  val sessionDate: LocalDate,
  val startTime: LocalTime? = null,
  val endTime: LocalTime? = null,
  val prisonerNumber: String,
  val bookingId: Long,
  val inCell: Boolean,
  val onWing: Boolean,
  val offWing: Boolean,
  val internalLocationId: Int? = null,
  val dpsLocationId: UUID? = null,
  val internalLocationCode: String? = null,
  val internalLocationDescription: String? = null,
  val scheduleDescription: String? = null,
  val activityId: Int,
  val activityCategory: String,
  val activitySummary: String? = null,
  val cancelled: Boolean,
  val suspended: Boolean,
  val autoSuspended: Boolean,
  val timeSlot: String,
  val issuePayment: Boolean? = null,
  val attendanceStatus: String? = null,
  val attendanceReasonCode: String? = null,
  val paidActivity: Boolean,
  val possibleAdvanceAttendance: Boolean,
) {
  fun toActivityScheduledInstanceForPrisoner() =
    ActivityScheduledInstanceForPrisoner(
      id = this.scheduledInstanceId,
      activityId = this.activityId,
      sessionDate = this.sessionDate,
      startTime = this.startTime,
      endTime = this.endTime,
      inCell = this.inCell,
      onWing = this.onWing,
      offWing = this.offWing,
      scheduleDescription = this.scheduleDescription,
      activityCategory = this.activityCategory,
      activitySummary = this.activitySummary,
      cancelled = this.cancelled,
      suspended = this.suspended,
      timeSlot = this.timeSlot,
      issuePayment = this.issuePayment,
      attendanceStatus = this.attendanceStatus,
      attendanceReasonCode = this.attendanceReasonCode,
      paidActivity = this.paidActivity,
      possibleAdvanceAttendance = this.possibleAdvanceAttendance,
    )
}
