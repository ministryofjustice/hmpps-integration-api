package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class PlannedSuspension(
  @Schema(description = "The planned start date of the suspension", example = "2020-01-02")
  val plannedStartDate: String,
  @Schema(description = "The planned end date of the suspension", example = "2020-01-04")
  val plannedEndDate: String?,
  @Schema(description = "Is the suspension paid or not", example = "true")
  val paid: Boolean,
)
