package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class Slot(
  @Schema(example = "123456")
  val id: Long,
  @Schema(example = "AM")
  val timeSlot: String,
  @Schema(example = "1")
  val weekNumber: Int,
  @Schema(example = "9:00")
  val startTime: String,
  @Schema(example = "11:30")
  val endTime: String,
  @Schema(example = "[Mon,Tue,Wed]")
  val daysOfWeek: String,
  @Schema(example = "true")
  val mondayFlag: Boolean,
  @Schema(example = "true")
  val tuesdayFlag: Boolean,
  @Schema(example = "true")
  val wednesdayFlag: Boolean,
  @Schema(example = "false")
  val thursdayFlag: Boolean,
  @Schema(example = "false")
  val fridayFlag: Boolean,
  @Schema(example = "false")
  val saturdayFlag: Boolean,
  @Schema(example = "false")
  val sundayFlag: Boolean,
)
