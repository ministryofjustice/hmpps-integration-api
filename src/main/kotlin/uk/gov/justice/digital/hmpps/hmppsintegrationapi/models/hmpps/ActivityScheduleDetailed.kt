package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class ActivityScheduleDetailed(
  val instances: List<ActivityScheduleInstance>,
  val allocations: List<ActivityScheduleAllocation>,
  val description: String,
  val suspensions: List<ActivityScheduleSuspension>,
  val internalLocation: Int?,
  val capacity: Int,
  val scheduleWeeks: Int,
  val slots: List<Slot>,
  val startDate: String,
  val endDate: String?,
  val runsOnBankHoliday: Boolean,
  val usePrisonRegimeTime: Boolean,
)
