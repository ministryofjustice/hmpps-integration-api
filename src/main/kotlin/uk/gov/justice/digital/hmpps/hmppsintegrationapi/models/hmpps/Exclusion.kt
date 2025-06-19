package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import java.time.DayOfWeek

data class Exclusion(
  @Schema(description = "The week of the schedule this slot relates to", example = "1")
  val weekNumber: Int,
  @Schema(description = "The time slot of the activity schedule", examples = ["AM", "PM", "ED"])
  val timeSlot: String,
  val monday: Boolean,
  val tuesday: Boolean,
  val wednesday: Boolean,
  val thursday: Boolean,
  val friday: Boolean,
  val saturday: Boolean,
  val sunday: Boolean,
  val customStartTime: String?,
  val customEndTime: String?,
  val daysOfWeek: List<DayOfWeek>,
)
