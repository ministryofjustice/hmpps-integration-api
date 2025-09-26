package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Slot
import java.time.DayOfWeek

data class ActivitiesSlot(
  val id: Long,
  val timeSlot: String,
  val weekNumber: Int,
  val startTime: String,
  val endTime: String,
  val daysOfWeek: List<String>,
  val mondayFlag: Boolean,
  val tuesdayFlag: Boolean,
  val wednesdayFlag: Boolean,
  val thursdayFlag: Boolean,
  val fridayFlag: Boolean,
  val saturdayFlag: Boolean,
  val sundayFlag: Boolean,
) {
  fun toSlot() =
    Slot(
      id = this.id,
      timeSlot = this.timeSlot,
      weekNumber = this.weekNumber,
      startTime = this.startTime,
      endTime = this.endTime,
      daysOfWeek = this.daysOfWeek,
      mondayFlag = this.mondayFlag,
      tuesdayFlag = this.tuesdayFlag,
      wednesdayFlag = this.wednesdayFlag,
      thursdayFlag = this.thursdayFlag,
      fridayFlag = this.fridayFlag,
      saturdayFlag = this.saturdayFlag,
      sundayFlag = this.sundayFlag,
    )

  fun getDaysOfWeek(): Set<DayOfWeek> =
    setOfNotNull(
      DayOfWeek.MONDAY.takeIf { mondayFlag },
      DayOfWeek.TUESDAY.takeIf { tuesdayFlag },
      DayOfWeek.WEDNESDAY.takeIf { wednesdayFlag },
      DayOfWeek.THURSDAY.takeIf { thursdayFlag },
      DayOfWeek.FRIDAY.takeIf { fridayFlag },
      DayOfWeek.SATURDAY.takeIf { saturdayFlag },
      DayOfWeek.SUNDAY.takeIf { sundayFlag },
    )
}
