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
  val attendanceReason: AttendanceReason? = null,
  @Schema(description = "Free text to allow comments to be put against the attendance", example = "Prisoner was too unwell to attend the activity.")
  val comment: String? = null,
  @Schema(description = "The attendance status", example = "WAITING")
  val status: String,
  @Schema(description = "The amount of pay awarded", example = "100")
  val payAmount: Int? = null,
  @Schema(description = "The amount of bonus awarded", example = "50")
  val bonusAmount: Int? = null,
  @Schema(description = "The number of pieces produced", example = "0")
  val pieces: Int? = null,
  @Schema(description = "Should payment be issued for SICK, REST or OTHER", example = "true")
  val issuePayment: Boolean? = null,
  @Schema(description = "Was an incentive level warning issued for REFUSED", example = "true")
  val incentiveLevelWarningIssued: Boolean? = null,
  @Schema(description = "Free text to allow other reasons for non attendance against the attendance", example = "Prisoner has a valid reason to miss the activity.")
  val otherAbsenceReason: String? = null,
  @Schema(description = "Free text for any case note entered against the attendance record", example = "Prisoner has refused to attend the activity without a valid reason to miss the activity.")
  val caseNoteText: String? = null,
  @Schema(description = "Whether the record is editable", example = "true")
  val editable: Boolean,
  @Schema(description = "Whether the record is payable", example = "true")
  val payable: Boolean,
)
