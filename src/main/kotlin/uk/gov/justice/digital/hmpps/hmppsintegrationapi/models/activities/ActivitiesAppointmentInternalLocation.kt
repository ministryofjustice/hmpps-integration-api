package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.InternalLocation

data class ActivitiesAppointmentInternalLocation(
  val id: Int,
  val prisonCode: String,
  val description: String,
) {
  fun toInternalLocation() =
    InternalLocation(
      code = this.prisonCode,
      description = this.description,
    )
}
