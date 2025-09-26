package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class RunningActivity(
  @Schema(description = "The internally-generated ID for this activity", example = "123456", required = true)
  val id: Long,
  @Schema(description = "The name of the activity", example = "Maths level 1")
  val activityName: String?,
  @Schema(description = "The category for this activity", required = true)
  val category: ActivityCategory,
  @Schema(description = "The capacity of the activity", example = "10", required = true)
  val capacity: Int,
  @Schema(description = "The number of prisoners currently allocated to the activity", example = "8", required = true)
  val allocated: Int,
  @Schema(description = "The number of prisoners currently on the waitlist for the activity", example = "1", required = true)
  val waitlisted: Int,
  @Schema(description = "Whether the activity is live or archived", example = "LIVE", required = true)
  val activityState: String,
)
