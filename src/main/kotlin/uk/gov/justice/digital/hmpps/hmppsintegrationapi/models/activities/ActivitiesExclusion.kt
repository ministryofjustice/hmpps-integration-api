package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

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
)
