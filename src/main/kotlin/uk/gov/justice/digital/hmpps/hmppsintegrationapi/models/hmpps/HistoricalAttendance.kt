package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class HistoricalAttendance(
  @Schema(description = "The internally-generated ID for this attendance", example = "123456")
  val id: Long,
  @Schema(description = "The schedule instance ID", example = "123456")
  val scheduleInstanceId: Long,
  @Schema(description = "The prisoner's number", example = "A1234AA")
  val prisonerNumber: String,
  @Schema(description = "The reason for attending or not")
  val attendanceReason: AttendanceReason?,
  @Schema(description = "Free text to allow comments to be put against the attendance", example = "Prisoner was too unwell to attend the activity.")
  val comment: String?,
  @Schema(description = "The date and time the attendance was updated", example = "2023-09-10T09:30:00")
  val recordedTime: String?,
  @Schema(description = "The username of the person who recorded the attendance", example = "A.JONES")
  val recordedBy: String?,
  @Schema(description = "The attendance status", example = "WAITING")
  val status: String,
  @Schema(description = "The amount of pay awarded", example = "100")
  val payAmount: Int?,
  @Schema(description = "The amount of bonus awarded", example = "50")
  val bonusAmount: Int?,
  @Schema(description = "The number of pieces produced", example = "0")
  val pieces: Int?,
  @Schema(description = "Should payment be issued for SICK, REST or OTHER", example = "true")
  val issuePayment: Boolean?,
  @Schema(description = "Was an incentive level warning issued for REFUSED", example = "true")
  val incentiveLevelWarningIssued: Boolean?,
  @Schema(description = "Free text to allow other reasons for non attendance against the attendance", example = "Prisoner has a valid reason to miss the activity.")
  val otherAbsenceReason: String?,
  @Schema(description = "Free text for any case note entered against the attendance record", example = "Prisoner has refused to attend the activity without a valid reason to miss the activity.")
  val caseNoteText: String?,
  @Schema(description = "Whether the record is editable", example = "true")
  val editable: Boolean,
  @Schema(description = "Whether the record is payable", example = "true")
  val payable: Boolean,
)
