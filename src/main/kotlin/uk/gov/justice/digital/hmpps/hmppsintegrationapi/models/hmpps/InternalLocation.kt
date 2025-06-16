package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class InternalLocation(
  @Schema(example = "EDU-ROOM-1")
  val code: String,
  @Schema(example = "Education - R1")
  val description: String,
)
