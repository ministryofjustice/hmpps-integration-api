package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class ActivityScheduleAllocation(
  val prisonerNumber: String,
  val activitySummary: String,
  val isUnemployment: Boolean,
  val prisonPayBand: PrisonPayBand?,
  val startDate: String,
  val endDate: String?,
  val allocatedTime: String?,
  val allocatedBy: String?,
  val deallocatedTime: String?,
  val deallocatedBy: String?,
  val deallocatedReason: ActivityReason?,
  val suspendedTime: String?,
  val suspendedBy: String?,
  val suspendedReason: String?,
  val status: String,
  val plannedDeallocation: PlannedDeallocation?,
  val plannedSuspension: PlannedSuspension?,
  val exclusions: List<Exclusion>,
)
