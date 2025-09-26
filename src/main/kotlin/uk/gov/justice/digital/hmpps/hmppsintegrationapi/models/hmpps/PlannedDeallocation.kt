package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class PlannedDeallocation(
  @Schema(description = "The planned de-allocation date", example = "2020-02-21")
  val plannedDate: String,
)
