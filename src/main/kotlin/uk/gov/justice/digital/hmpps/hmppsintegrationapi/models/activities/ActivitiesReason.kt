package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivityReason

data class ActivitiesReason(
  val code: String,
  val description: String,
) {
  fun toActivityReason() =
    ActivityReason(
      code = this.code,
      description = this.description,
    )
}
