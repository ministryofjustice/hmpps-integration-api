package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesInternalLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesSlot

data class ActivitySchedule(
  val id: Long,
  val description: String,
  val internalLocation: ActivitiesInternalLocation?,
  val capacity: Int,
  val scheduleWeeks: Int,
  val slots: List<ActivitiesSlot>,
  val startDate: String,
  val endDate: String?,
  val usePrisonRegimeTime: Boolean,
)
