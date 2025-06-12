package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

data class ActivitiesAttendee(
  val appointmentAttendeeId: Long,
  val prisonerNumber: String,
  val bookingId: Long,
)
