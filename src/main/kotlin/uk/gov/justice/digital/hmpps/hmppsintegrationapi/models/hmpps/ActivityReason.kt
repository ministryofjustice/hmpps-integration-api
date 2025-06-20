package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class ActivityReason(
  @Schema(description = "The code for the reason", example = "RELEASED")
  val code: String,
  @Schema(description = "The description for the reason", example = "Released from prison")
  val description: String,
)
