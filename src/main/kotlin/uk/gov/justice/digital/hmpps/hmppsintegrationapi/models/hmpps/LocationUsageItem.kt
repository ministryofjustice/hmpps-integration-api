package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class LocationUsageItem(
  @Schema(description = "Usage Type", examples = ["ADJUDICATION_HEARING", "APPOINTMENT", "MOVEMENT", "OCCURRENCE", "PROGRAMMES_ACTIVITIES", "PROPERTY", "VISIT", "OTHER"])
  val usageType: String,
  @Schema(description = "Capacity")
  val capacity: Int?,
  @Schema(description = "Sequence")
  val sequence: Int,
)
