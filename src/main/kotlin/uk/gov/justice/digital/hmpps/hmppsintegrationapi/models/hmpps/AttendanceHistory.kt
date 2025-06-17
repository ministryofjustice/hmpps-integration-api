package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class AttendanceHistory(
  val id: Long,
  val attendanceReason: AttendanceReason,
  val comment: String?,
  val recordedTime: String,
  val recordedBy: String,
  val issuePayment: Boolean?,
  val incentiveLevelWarningIssued: Boolean?,
  val otherAbsenceReason: String?,
  val caseNoteText: String?,
)
