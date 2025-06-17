package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class Attendance(
  val id: Long,
  val scheduledInstanceId: Long,
  val prisonerNumber: String,
  val attendanceReason: AttendanceReason?,
  val comment: String?,
  val recordedTime: String?,
  val recordedBy: String?,
  val status: String,
  val payAmount: Int?,
  val bonusAmount: Int?,
  val pieces: Int?,
  val issuePayment: Boolean?,
  val incentiveLevelWarningIssued: Boolean?,
  val otherAbsenceReason: String?,
  val caseNoteText: String?,
  val attendanceHistory: List<AttendanceHistory>?,
  val editable: Boolean,
  val payable: Boolean,
)
