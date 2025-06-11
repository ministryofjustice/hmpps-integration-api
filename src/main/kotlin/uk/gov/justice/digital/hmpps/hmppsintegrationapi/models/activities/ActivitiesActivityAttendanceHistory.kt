package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

data class ActivitiesActivityAttendanceHistory(
  val id: Long,
  val attendanceReason: ActivitiesActivityAttendanceReason,
  val comment: String?,
  val recordedTime: String,
  val recordedBy: String,
  val issuePayment: Boolean?,
  val incentiveLevelWarningIssued: Boolean?,
  val otherAbsenceReason: String?,
  val caseNoteText: String?,
)
