package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class LocationCapacity(
  @Schema(description = "Max capacity of the location", example = "2")
  val maxCapacity: Int,
  @Schema(description = "Working capacity of the location", example = "2")
  val workingCapacity: Int,
)
