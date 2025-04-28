package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class ResidentialDetails(
  val topLevelLocationType: String,
  val subLocationName: String?,
  val parentLocation: Location?,
  val subLocations: List<Location>,
)
