package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class Slot(
  @Schema(example = "123456", description = "Unique ID of the slot")
  val id: Long,
  @Schema(example = "AM", description = "Time slot code (e.g., AM, PM, ED)")
  val timeSlot: String,
  @Schema(example = "1", description = "Week number for the slot")
  val weekNumber: Int,
  @Schema(example = "9:00", description = "Start time of the slot")
  val startTime: String,
  @Schema(example = "11:30", description = "End time of the slot")
  val endTime: String,
  @Schema(example = "['Mon', 'Tue', 'Wed']", description = "String representation of the days this slot occurs on")
  val daysOfWeek: List<String>,
  @Schema(example = "true", description = "Flag indicating slot occurs on Monday")
  val mondayFlag: Boolean,
  @Schema(example = "true", description = "Flag indicating slot occurs on Tuesday")
  val tuesdayFlag: Boolean,
  @Schema(example = "true", description = "Flag indicating slot occurs on Wednesday")
  val wednesdayFlag: Boolean,
  @Schema(example = "false", description = "Flag indicating slot occurs on Thursday")
  val thursdayFlag: Boolean,
  @Schema(example = "false", description = "Flag indicating slot occurs on Friday")
  val fridayFlag: Boolean,
  @Schema(example = "false", description = "Flag indicating slot occurs on Saturday")
  val saturdayFlag: Boolean,
  @Schema(example = "false", description = "Flag indicating slot occurs on Sunday")
  val sundayFlag: Boolean,
)
