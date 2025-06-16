package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivitySchedule

data class ActivitiesActivitySchedule(
  val id: Long,
  val description: String,
  val internalLocation: ActivitiesInternalLocation?,
  val capacity: Int,
  val activity: ActivitiesActivity,
  val scheduleWeeks: Int,
  val slots: List<ActivitiesSlot>,
  val startDate: String,
  val endDate: String?,
  val usePrisonRegimeTime: Boolean,
) {
  fun toActivitySchedule() =
    ActivitySchedule(
      scheduleId = this.id,
      description = this.description,
      internalLocation = this.internalLocation,
      capacity = this.capacity,
      scheduleWeeks = this.scheduleWeeks,
      slots = this.slots,
      startDate = this.startDate,
      endDate = this.endDate,
      usePrisonRegimeTime = this.usePrisonRegimeTime,
    )
}
