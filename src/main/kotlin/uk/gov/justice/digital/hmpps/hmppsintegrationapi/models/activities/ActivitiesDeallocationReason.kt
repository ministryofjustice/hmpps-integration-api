package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DeallocationReason

data class ActivitiesDeallocationReason(
  val code: String,
  val description: String,
) {
  fun toDeallocationReason() =
    DeallocationReason(
      code = this.code,
      description = this.description,
    )
}
