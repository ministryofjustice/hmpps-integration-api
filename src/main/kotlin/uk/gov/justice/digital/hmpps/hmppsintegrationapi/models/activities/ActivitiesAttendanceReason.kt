package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AttendanceReason
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ReasonForAttendance

data class ActivitiesAttendanceReason(
  val id: Long,
  val code: String,
  val description: String,
  val attended: Boolean,
  val capturePay: Boolean,
  val captureMoreDetail: Boolean,
  val captureCaseNote: Boolean,
  val captureIncentiveLevelWarning: Boolean,
  val captureOtherText: Boolean,
  val displayInAbsence: Boolean,
  val displaySequence: Int?,
  val notes: String,
) {
  fun toAttendanceReason(): AttendanceReason =
    AttendanceReason(
      code = this.code,
      description = this.description,
      attended = this.attended,
      capturePay = this.capturePay,
      captureMoreDetail = this.captureMoreDetail,
      captureCaseNote = this.captureCaseNote,
      captureIncentiveLevelWarning = this.captureIncentiveLevelWarning,
      captureOtherText = this.captureOtherText,
      displayInAbsence = this.displayInAbsence,
      notes = this.notes,
    )

  fun toReasonForAttendance(): ReasonForAttendance =
    ReasonForAttendance(
      id = this.id,
      code = this.code,
      description = this.description,
      attended = this.attended,
      notes = this.notes,
    )
}
