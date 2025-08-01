package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class PrisonPayBand(
  @Schema(description = "The internally-generated ID for this prison pay band", example = "123456", required = true)
  val id: Long,
  @Schema(description = "The alternative text to use in place of the description", examples = ["Low", "Medium", "High"], required = true)
  val alias: String,
  @Schema(description = "The description of pay band in this prison", example = "Pay band 1", required = true)
  val description: String,
)
