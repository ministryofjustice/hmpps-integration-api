package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Exclusion
import java.time.DayOfWeek

data class ActivitiesExclusion(
  val weekNumber: Int,
  val timeSlot: String,
  val monday: Boolean,
  val tuesday: Boolean,
  val wednesday: Boolean,
  val thursday: Boolean,
  val friday: Boolean,
  val saturday: Boolean,
  val sunday: Boolean,
  val customStartTime: String?,
  val customEndTime: String?,
  val daysOfWeek: List<DayOfWeek>,
) {
  fun toExclusion() =
    Exclusion(
      weekNumber = this.weekNumber,
      timeSlot = this.timeSlot,
      monday = this.monday,
      tuesday = this.tuesday,
      wednesday = this.wednesday,
      thursday = this.thursday,
      friday = this.friday,
      saturday = this.saturday,
      sunday = this.sunday,
      customStartTime = this.customStartTime,
      customEndTime = this.customEndTime,
      daysOfWeek = this.daysOfWeek,
    )
}
