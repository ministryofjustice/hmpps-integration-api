package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AdvanceAttendance

data class ActivitiesAdvanceAttendance(
  val id: Long,
  val scheduleInstanceId: Long,
  val prisonerNumber: String,
  val issuePayment: Boolean? = null,
  val payAmount: Int? = null,
  val recordedTime: String?,
  val recordedBy: String?,
  val attendanceHistory: List<ActivitiesAdvanceAttendanceHistory>?,
) {
  fun toAdvanceAttendance() =
    AdvanceAttendance(
      id = this.id,
      scheduleInstanceId = this.scheduleInstanceId,
      prisonerNumber = this.prisonerNumber,
      issuePayment = this.issuePayment,
      payAmount = this.payAmount,
      recordedTime = this.recordedTime,
      recordedBy = this.recordedBy,
      attendanceHistory = this.attendanceHistory?.map { it.toAdvanceAttendanceHistory() },
    )
}
