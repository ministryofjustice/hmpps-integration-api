package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

data class ActivitiesScheduledEvents(
  val prisonCode: String?,
  val prisonerNumbers: List<String>?,
  val startDate: String?,
  val endDate: String?,
  val appointments: List<ActivitiesAppointment>?,
  val courtHearings: List<ActivitiesAppointment>?,
  val visits: List<ActivitiesAppointment>?,
  val activities: List<ActivitiesAppointment>?,
  val externalTransfers: List<ActivitiesAppointment>?,
  val adjudications: List<ActivitiesAppointment>?,
)
