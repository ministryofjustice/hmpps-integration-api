package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class ActivityScheduleInstance(
  @Schema(description = "The internally-generated ID for this scheduled instance", example = "123456")
  val scheduleInstanceId: Long,
  @Schema(description = "The specific date for this scheduled instance", example = "2020-01-31")
  val date: String,
  @Schema(description = "The start time for this scheduled instance", example = "09:00")
  val startTime: String,
  @Schema(description = "The end time for this scheduled instance", example = "13:00")
  val endTime: String,
  @Schema(description = "The timeslot", examples = ["AM", "PM", "ED"])
  val timeSlot: String,
  @Schema(description = "Flag to indicate if this scheduled instance has been cancelled since being scheduled", example = "true")
  val cancelled: Boolean,
  @Schema(description = "Date and time this scheduled instance was cancelled (or null if not cancelled)", example = "2022-09-29T11:20:00")
  val cancelledTime: String?,
  @Schema(description = "The attendance records for this scheduled instance")
  val attendances: List<Attendance>,
)
