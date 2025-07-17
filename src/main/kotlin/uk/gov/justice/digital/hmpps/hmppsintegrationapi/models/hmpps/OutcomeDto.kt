package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class OutcomeDto(
  @Schema(description = "Outcome code", example = "REFER_POLICE")
  val code: String? = null,
  @Schema(description = "Outcome details")
  val details: String? = null,
  @Schema(description = "Optional not proceeded with reason", example = "ANOTHER_WAY")
  val reason: String? = null,
  @Schema(description = "Quashed reason", example = "FLAWED_CASE")
  val quashedReason: String? = null,
  @Schema(description = "Flag to indicate if the outcome can be removed", example = "false")
  val canRemove: Boolean? = null,
)
