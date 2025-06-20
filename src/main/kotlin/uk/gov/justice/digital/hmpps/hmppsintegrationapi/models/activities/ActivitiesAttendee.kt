package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Attendee

data class ActivitiesAttendee(
  val appointmentAttendeeId: Long,
  val prisonerNumber: String,
  val bookingId: Long,
) {
  fun toAttendee() =
    Attendee(
      prisonerNumber = this.prisonerNumber,
    )
}
