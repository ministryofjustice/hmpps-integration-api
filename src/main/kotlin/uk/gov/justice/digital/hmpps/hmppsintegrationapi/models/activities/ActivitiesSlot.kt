package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

data class ActivitiesSlot(
  val id: Long,
  val timeSlot: String,
  val weekNumber: Int,
  val startTime: String,
  val endTime: String,
  val daysOfWeek: String,
  val mondayFlag: Boolean,
  val tuesdayFlag: Boolean,
  val wednesdayFlag: Boolean,
  val thursdayFlag: Boolean,
  val fridayFlag: Boolean,
  val saturdayFlag: Boolean,
  val sundayFlag: Boolean,
)
