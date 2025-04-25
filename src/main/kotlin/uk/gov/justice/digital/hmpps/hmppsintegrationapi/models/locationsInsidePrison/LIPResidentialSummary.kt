package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison

data class LIPResidentialSummary(
  val prisonSummary: LIPPrisonSummary?,
  val topLevelLocationType: String,
  val subLocationName: String?,
  val locationHierarchy: List<LIPLocationHierarchyItem>,
  val parentLocation: LIPLocation?,
  val subLocations: List<LIPLocation>,
)
