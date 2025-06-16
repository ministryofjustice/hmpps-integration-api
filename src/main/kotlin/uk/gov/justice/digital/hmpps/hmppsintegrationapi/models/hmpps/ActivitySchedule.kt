package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesInternalLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesSlot

data class ActivitySchedule(
  @Schema(example = "1162")
  val id: Long,
  @Schema(example = "Monday AM Houseblock 3")
  val description: String,
  val internalLocation: ActivitiesInternalLocation?,
  @Schema(example = "10")
  val capacity: Int,
  @Schema(example = "1")
  val scheduleWeeks: Int,
  val slots: List<ActivitiesSlot>,
  @Schema(example = "2022-09-21")
  val startDate: String,
  @Schema(example = "2022-10-21")
  val endDate: String?,
  @Schema(example = "true")
  val usePrisonRegimeTime: Boolean,
)
