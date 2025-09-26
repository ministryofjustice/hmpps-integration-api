package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivityScheduleSuspension

data class ActivitiesActivityScheduleSuspension(
  val suspendedFrom: String,
  val suspendedUntil: String?,
) {
  fun toActivityScheduleSuspension() =
    ActivityScheduleSuspension(
      suspendedFrom = this.suspendedFrom,
      suspendedUntil = this.suspendedUntil,
    )
}
