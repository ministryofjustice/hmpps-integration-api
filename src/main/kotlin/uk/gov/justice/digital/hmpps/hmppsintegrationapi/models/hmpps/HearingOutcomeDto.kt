package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class HearingOutcomeDto(
  @Schema(description = "The hearing outcome code", example = "COMPLETE")
  val code: String? = null,
  @Schema(description = "The reason for the outcome", example = "LEGAL_ADVICE")
  val reason: String? = null,
  @Schema(description = "The details of the outcome")
  val details: String? = null,
  @Schema(description = "Hearing outcome plea", example = "UNFIT")
  val plea: String? = null,
)
