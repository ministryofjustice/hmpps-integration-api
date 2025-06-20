package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class ActivityScheduleDetailed(
  @Schema(description = "The planned instances associated with this activity schedule")
  val instances: List<ActivityScheduleInstance>,
  @Schema(description = "The list of allocated prisoners who are allocated to this schedule, at this time and location")
  val allocations: List<ActivityScheduleAllocation>,
  @Schema(description = "The description of this activity schedule", example = "Entry level Maths 1")
  val description: String,
  @Schema(description = "Indicates the dates between which the schedule has been suspended")
  val suspensions: List<ActivityScheduleSuspension>,
  @Schema(description = "The NOMIS internal location id for this schedule")
  val internalLocation: Int?, // TODO: We've been not providing the location ID. need to decide what to do here
  @Schema(description = "The maximum number of prisoners allowed for a scheduled instance of this schedule", example = "10")
  val capacity: Int,
  @Schema(description = "The number of weeks in the schedule", example = "3")
  val scheduleWeeks: Int,
  @Schema(description = "The slots associated with this activity schedule")
  val slots: List<Slot>,
  @Schema(description = "The date on which this schedule will start. From this date, any schedules will be created as real, planned instances", example = "2020-01-31")
  val startDate: String,
  @Schema(description = "The date on which this schedule will end. From this date, any schedules will be created as real, planned instances", example = "2020-10-31")
  val endDate: String?,
  @Schema(description = "Whether the schedule runs on bank holidays", example = "true")
  val runsOnBankHoliday: Boolean,
  @Schema(description = "A flag to indicate if this activity is scheduled according to prison standard regime times", example = "true")
  val usePrisonRegimeTime: Boolean,
)
