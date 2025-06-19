package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class ActivityScheduleSuspension(
  @Schema(description = "The date from which the activity schedule was suspended", example = "2020-01-23")
  val suspendedFrom: String,
  @Schema(description = "The date until which the activity schedule was suspended. If null, the schedule is suspended indefinitely", example = "2020-01-24")
  val suspendedUntil: String?,
)
