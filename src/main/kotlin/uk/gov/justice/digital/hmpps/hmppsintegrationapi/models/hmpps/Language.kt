package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class Language(
  @Schema(description = "Type of language (e.g., primary, secondary)", example = "PRIM")
  val type: String? = null,
  @Schema(description = "Language code (e.g., ENG for English)", example = "ENG")
  val code: String? = null,
  @Schema(description = "Read skill level (e.g., Y for yes, N for no)", example = "Y")
  val readSkill: String? = null,
  @Schema(description = "Write skill level (e.g., Y for yes, N for no)", example = "Y")
  val writeSkill: String? = null,
  @Schema(description = "Speak skill level (e.g., Y for yes, N for no)", example = "Y")
  val speakSkill: String? = null,
  @Schema(description = "Interpreter requested", example = "true")
  val interpreterRequested: Boolean? = null,
)
