package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class InternalLocation(
  @Schema(example = "EDU-ROOM-1", description = "Location code used to identify the internal area")
  val code: String,
  @Schema(example = "Education - R1", description = "Human-readable description of the internal location")
  val description: String,
)
