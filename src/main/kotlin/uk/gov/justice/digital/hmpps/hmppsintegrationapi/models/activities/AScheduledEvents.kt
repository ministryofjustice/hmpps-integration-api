package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

data class AScheduledEvents(
  val prisonCode: String,
  val prisonerNumbers: List<String>,
  val startDate: String,
  val endDate: String,
  val appointments: List<AAppointment>,
  val courtHearings: List<AAppointment>,
  val visits: List<AAppointment>,
  val activities: List<AAppointment>,
  val externalTransfers: List<AAppointment>,
  val adjudications: List<AAppointment>,
)
