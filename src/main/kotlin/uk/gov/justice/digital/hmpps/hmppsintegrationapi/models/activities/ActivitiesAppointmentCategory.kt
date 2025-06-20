package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AppointmentCategory

data class ActivitiesAppointmentCategory(
  val code: String,
  val description: String,
) {
  fun toAppointmentCategory() =
    AppointmentCategory(
      code = this.code,
      description = this.description,
    )
}
