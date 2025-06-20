package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@Schema(description = "Describes a case note to be added to a prisoner's profile")
data class AddCaseNoteRequest(
  @Schema(example = "GEN", required = true, description = "Case Note Type")
  @field:NotNull(message = "The case note type must be supplied.")
  val type: String,
  @Schema(description = "The text which will appear on the case note.")
  @field:NotEmpty(message = "The case note text must be supplied.")
  @field:Size(max = 3800, message = "The case note text should not exceed {max} characters")
  val text: String? = null,
)
