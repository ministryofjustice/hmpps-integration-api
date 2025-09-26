package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class ActivityCategory(
  @Schema(description = "The activity category code", example = "LEISURE_SOCIAL", required = true)
  val code: String,
  @Schema(description = "The name of the activity category", example = "Leisure and social", required = true)
  val name: String,
  @Schema(description = "The description of the activity category", example = "Library time and social clubs, like music or art")
  val description: String?,
)
