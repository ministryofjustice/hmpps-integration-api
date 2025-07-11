package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class Status(
  @Schema(example = "ok", description = "API Service is running")
  val status: String = "ok",
)
