package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class ReasonForAttendance(
  @Schema(description = "The id for the (non) attendance reason", example = "1")
  val id: Long,
  @Schema(description = "The code for the (non) attendance reason", example = "SICK")
  val code: String,
  @Schema(description = "The description of the (non) attendance reason", example = "Sick")
  val description: String,
  @Schema(description = "A flag to show whether the reason is Attended (true) or Not Attended (false)", example = "true")
  val attended: Boolean,
  @Schema(description = "Any internal notes to explain the use of the reason", example = "Maps to ACCAB in NOMIS")
  val notes: String,
)
