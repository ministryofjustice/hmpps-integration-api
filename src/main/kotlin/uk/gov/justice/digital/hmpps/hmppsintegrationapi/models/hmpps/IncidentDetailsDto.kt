package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class IncidentDetailsDto(
  val locationId: Number? = null,
  val dateTimeOfIncident: String? = null,
  val dateTimeOfDiscovery: String? = null,
  val handoverDeadline: String? = null,
)
