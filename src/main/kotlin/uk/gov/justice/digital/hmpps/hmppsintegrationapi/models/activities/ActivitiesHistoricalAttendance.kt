package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HistoricalAttendance

data class ActivitiesHistoricalAttendance(
  val id: Long,
  val scheduleInstanceId: Long,
  val prisonerNumber: String,
  val attendanceReason: ActivitiesAttendanceReason? = null,
  val comment: String? = null,
  val status: String,
  val payAmount: Int? = null,
  val bonusAmount: Int? = null,
  val pieces: Int? = null,
  val issuePayment: Boolean? = null,
  val incentiveLevelWarningIssued: Boolean? = null,
  val otherAbsenceReason: String? = null,
  val caseNoteText: String? = null,
  val attendanceHistory: List<ActivitiesAttendanceHistory>? = null,
  val editable: Boolean,
  val payable: Boolean,
) {
  fun toHistoricalAttendance(): HistoricalAttendance =
    HistoricalAttendance(
      id = this.id,
      scheduleInstanceId = this.scheduleInstanceId,
      prisonerNumber = this.prisonerNumber,
      attendanceReason = this.attendanceReason?.toAttendanceReason(),
      comment = this.comment,
      status = this.status,
      payAmount = this.payAmount,
      bonusAmount = this.bonusAmount,
      pieces = this.pieces,
      issuePayment = this.issuePayment,
      incentiveLevelWarningIssued = this.incentiveLevelWarningIssued,
      otherAbsenceReason = this.otherAbsenceReason,
      caseNoteText = this.caseNoteText,
      editable = this.editable,
      payable = this.payable,
    )
}
