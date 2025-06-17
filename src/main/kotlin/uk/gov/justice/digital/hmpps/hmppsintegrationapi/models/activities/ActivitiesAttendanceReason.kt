package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

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
)
