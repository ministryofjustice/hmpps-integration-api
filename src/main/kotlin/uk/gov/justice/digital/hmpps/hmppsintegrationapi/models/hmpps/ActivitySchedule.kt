package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class ActivitySchedule(
  @Schema(example = "1162", description = "Unique ID of the activity schedule")
  val id: Long,
  @Schema(example = "Monday AM Houseblock 3", description = "Description of the scheduled activity")
  val description: String,
  val internalLocation: InternalLocation?,
  @Schema(example = "10", description = "Maximum number of participants")
  val capacity: Int,
  @Schema(example = "1", description = "Number of weeks the schedule repeats")
  val scheduleWeeks: Int,
  val slots: List<Slot>,
  @Schema(example = "2022-09-21", description = "Start date of the schedule (YYYY-MM-DD)")
  val startDate: String,
  @Schema(example = "2022-10-21", description = "End date of the schedule (YYYY-MM-DD)")
  val endDate: String?,
  @Schema(example = "true", description = "Whether to use prison regime time")
  val usePrisonRegimeTime: Boolean,
)
