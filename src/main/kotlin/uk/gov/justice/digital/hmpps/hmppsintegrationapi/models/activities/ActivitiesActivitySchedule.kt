package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivitySchedule
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.InternalLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Slot

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
      id = this.id,
      description = this.description,
      internalLocation = this.internalLocation?.let { InternalLocation(it.code, it.description) },
      capacity = this.capacity,
      scheduleWeeks = this.scheduleWeeks,
      slots = this.slots.map { Slot(it.id, it.timeSlot, it.weekNumber, it.startTime, it.endTime, it.daysOfWeek, it.mondayFlag, it.tuesdayFlag, it.wednesdayFlag, it.thursdayFlag, it.fridayFlag, it.saturdayFlag, it.sundayFlag) },
      startDate = this.startDate,
      endDate = this.endDate,
      usePrisonRegimeTime = this.usePrisonRegimeTime,
    )
}
