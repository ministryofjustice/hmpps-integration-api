package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

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
)
