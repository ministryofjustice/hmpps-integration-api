package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

data class ActivitiesActivityScheduleDetailed(
  val id: Long,
  val instances: List<ActivitiesActivityScheduleInstance>,
  val allocations: List<ActivitiesActivityScheduleAllocation>,
  val description: String,
  val suspensions: List<ActivitiesActivityScheduleSuspension>,
  val internalLocation: ActivitiesInternalLocation?,
  val capacity: Int,
  val activity: ActivitiesActivity,
  val scheduleWeeks: Int,
  val slots: List<ActivitiesSlot>,
  val startDate: String,
  val endDate: String?,
  val runsOnBankHoliday: Boolean,
  val updatedTime: String?,
  val updatedBy: String?,
  val usePrisonRegimeTime: Boolean,
)
