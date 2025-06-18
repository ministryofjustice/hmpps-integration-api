package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class AttendanceReason(
  @Schema(description = "The code for the (non) attendance reason", example = "SICK")
  val code: String,
  @Schema(description = "The description of the (non) attendance reason", example = "Sick")
  val description: String,
  @Schema(description = "A flag to show whether the reason is Attended (true) or Not Attended (false)", example = "true")
  val attended: Boolean,
  @Schema(description = "A flag to show whether or not to capture whether the prisoner should still be paid", example = "true")
  val capturePay: Boolean,
  @Schema(description = "A flag to show whether or not to capture more detail", example = "true")
  val captureMoreDetail: Boolean,
  @Schema(description = "A flag to show whether or not to capture a case note", example = "true")
  val captureCaseNote: Boolean,
  @Schema(description = "A flag to show whether or not to capture whether an incentive level warning has been issued due to non attendance", example = "false")
  val captureIncentiveLevelWarning: Boolean,
  @Schema(description = "A flag to show whether or not to capture other text", example = "false")
  val captureOtherText: Boolean,
  @Schema(description = "A flag to show whether or not the reason should be displayed in the UI as an option for non attendance", example = "false")
  val displayInAbsence: Boolean,
  @Schema(description = "The sequence in which the reason should be displayed in the UI", example = "1")
  val displaySequence: Int?,
  @Schema(description = "Any internal notes to explain the use of the reason", example = "Maps to ACCAB in NOMIS")
  val notes: String,
)
