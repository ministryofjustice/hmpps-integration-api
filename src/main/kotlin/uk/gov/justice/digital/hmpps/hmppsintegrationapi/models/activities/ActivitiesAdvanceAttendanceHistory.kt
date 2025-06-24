package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AdvanceAttendanceHistory

data class ActivitiesAdvanceAttendanceHistory(
  val id: Long,
  val issuePayment: Boolean,
  val recordedTime: String,
  val recordedBy: String,
) {
  fun toAdvanceAttendanceHistory() =
    AdvanceAttendanceHistory(
      id = this.id,
      issuePayment = this.issuePayment,
      recordedTime = this.recordedTime,
      recordedBy = this.recordedBy,
    )
}
