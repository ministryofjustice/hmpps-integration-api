package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class OffenceDto(
  @Schema(description = "The offence code", example = "3")
  val offenceCode: Number? = null,
  @Schema(description = "The offence rules they have broken")
  val offenceRule: OffenceRuleDto? = null,
)
