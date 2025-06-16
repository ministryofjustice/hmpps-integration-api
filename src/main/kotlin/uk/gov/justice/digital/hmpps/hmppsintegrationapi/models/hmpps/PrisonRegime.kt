package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import java.time.DayOfWeek

data class PrisonRegime(
  @Schema(description = "The start time for the am slot", example = "09:00", required = true)
  val amStart: String,
  @Schema(description = "The end time for the am slot", example = "12:00", required = true)
  val amFinish: String,
  @Schema(description = "The start time for the pm slot", example = "13:00", required = true)
  val pmStart: String,
  @Schema(description = "The end time for the pm slot", example = "17:00", required = true)
  val pmFinish: String,
  @Schema(description = "The start time for the ed slot", example = "18:00", required = true)
  val edStart: String,
  @Schema(description = "The end time for the ed slot", example = "21:00", required = true)
  val edFinish: String,
  @Schema(description = "The day of week the regime is applicable to", example = "MONDAY", required = true)
  val dayOfWeek: DayOfWeek,
)
