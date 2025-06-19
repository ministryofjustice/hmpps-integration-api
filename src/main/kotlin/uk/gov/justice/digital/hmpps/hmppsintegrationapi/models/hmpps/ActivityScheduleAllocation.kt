package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class ActivityScheduleAllocation(
  @Schema(description = "The prisoner number", example = "A1234AA")
  val prisonerNumber: String,
  @Schema(description = "A summary of the activity")
  val activitySummary: String,
  @Schema(description = "Indicates whether this allocation is to an activity within the 'Not in work' category", example = "true")
  val isUnemployment: Boolean,
  @Schema(description = "Where a prison uses pay bands to differentiate earnings, this is the pay band given to this prisoner. Will be null for unpaid activities.")
  val prisonPayBand: PrisonPayBand?,
  @Schema(description = "The date the prisoner will start the activity", example = "2022-09-10")
  val startDate: String,
  @Schema(description = "The date the prisoner will stop attending the activity", example = "2022-10-10")
  val endDate: String?,
  @Schema(description = "The date and time the prisoner was allocated to the activity", example = "2022-09-01T09:00:00")
  val allocatedTime: String?,
  @Schema(description = "The date and time the prisoner was deallocated from the activity", example = "2022-09-01T09:00:00")
  val deallocatedTime: String?,
  @Schema(description = "The code and descriptive reason why this prisoner was deallocated from the activity")
  val deallocatedReason: ActivityReason?,
  @Schema(description = "The date and time the allocation was suspended", example = "2022-09-01T09:00:00")
  val suspendedTime: String?,
  @Schema(description = "The descriptive reason why this prisoner was suspended from the activity", example = "Temporarily released from prison")
  val suspendedReason: String?,
  @Schema(description = "The status of the allocation. Note that SUSPENDED is suspended without pay.", example = "ACTIVE")
  val status: String,
  @Schema(description = "Where an allocation end date has been set, this includes the details of the planned de-allocation date")
  val plannedDeallocation: PlannedDeallocation?,
  @Schema(description = "This includes the details of the planned suspension dates for the allocation if there is one")
  val plannedSuspension: PlannedSuspension?,
  @Schema(description = "The days and times that the prisoner is excluded from this activity's schedule. All values must match a slot where the activity is scheduled to run. There can be exclusions defined on the same day and time slot over multiple weeks.")
  val exclusions: List<Exclusion>,
)
