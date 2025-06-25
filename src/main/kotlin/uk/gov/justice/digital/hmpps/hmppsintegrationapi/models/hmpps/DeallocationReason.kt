package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class DeallocationReason(
  @Schema(description = "The reason code for deallocation", example = "RELEASED")
  val code: String,
  @Schema(description = "A description of the deallocation reason", example = "Released from prison")
  val description: String,
)
