package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivityScheduleDetailed

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
) {
  fun toActivityScheduleDetailed() =
    ActivityScheduleDetailed(
      instances = this.instances.map { it.toActivityScheduleInstance() },
      allocations = this.allocations.map { it.toActivityScheduleAllocation() },
      description = this.description,
      suspensions = this.suspensions.map { it.toActivityScheduleSuspension() },
      internalLocation = this.internalLocation?.id,
      capacity = this.capacity,
      scheduleWeeks = this.scheduleWeeks,
      slots = this.slots.map { it.toSlot() },
      startDate = this.startDate,
      endDate = this.endDate,
      runsOnBankHoliday = this.runsOnBankHoliday,
      usePrisonRegimeTime = this.usePrisonRegimeTime,
    )
}
