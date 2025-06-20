package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AttendanceHistory

data class ActivitiesAttendanceHistory(
  val id: Long,
  val attendanceReason: ActivitiesAttendanceReason,
  val comment: String?,
  val recordedTime: String,
  val recordedBy: String,
  val issuePayment: Boolean?,
  val incentiveLevelWarningIssued: Boolean?,
  val otherAbsenceReason: String?,
  val caseNoteText: String?,
) {
  fun toAttendanceHistory(): AttendanceHistory =
    AttendanceHistory(
      id = this.id,
      attendanceReason = this.attendanceReason.toAttendanceReason(),
      comment = this.comment,
      recordedTime = this.recordedTime,
      issuePayment = this.issuePayment,
      incentiveLevelWarningIssued = this.incentiveLevelWarningIssued,
      otherAbsenceReason = this.otherAbsenceReason,
      caseNoteText = this.caseNoteText,
    )
}
