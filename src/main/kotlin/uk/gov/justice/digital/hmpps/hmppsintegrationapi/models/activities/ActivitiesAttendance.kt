package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Attendance

data class ActivitiesAttendance(
  val id: Long,
  val scheduledInstanceId: Long,
  val prisonerNumber: String,
  val attendanceReason: ActivitiesAttendanceReason?,
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
  val attendanceHistory: List<ActivitiesAttendanceHistory>?,
  val editable: Boolean,
  val payable: Boolean,
) {
  fun toAttendance(): Attendance =
    Attendance(
      id = this.id,
      scheduledInstanceId = this.scheduledInstanceId,
      prisonerNumber = this.prisonerNumber,
      attendanceReason = this.attendanceReason?.toAttendanceReason(),
      comment = this.comment,
      recordedTime = this.recordedTime,
      recordedBy = this.recordedBy,
      status = this.status,
      payAmount = this.payAmount,
      bonusAmount = this.bonusAmount,
      pieces = this.pieces,
      issuePayment = this.issuePayment,
      incentiveLevelWarningIssued = this.incentiveLevelWarningIssued,
      otherAbsenceReason = this.otherAbsenceReason,
      caseNoteText = this.caseNoteText,
      attendanceHistory = this.attendanceHistory?.map { it.toAttendanceHistory() },
      editable = this.editable,
      payable = this.payable,
    )
}
