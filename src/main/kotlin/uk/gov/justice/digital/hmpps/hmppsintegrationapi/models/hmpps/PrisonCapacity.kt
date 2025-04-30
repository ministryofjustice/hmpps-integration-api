package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class PrisonCapacity(
  @Schema(description = "Max capacity of the location", example = "1000")
  val maxCapacity: Int,
  @Schema(description = "Signed capacity of the location", example = "1000")
  val signedCapacity: Int,
  @Schema(description = "Working capacity of the location", example = "1000")
  val workingCapacity: Int,
)
