package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ResidentialDetails

data class LIPResidentialSummary(
  val prisonSummary: LIPPrisonSummary?,
  val topLevelLocationType: String,
  val subLocationName: String?,
  val locationHierarchy: List<LIPLocationHierarchyItem>,
  val parentLocation: LIPLocation?,
  val subLocations: List<LIPLocation>,
) {
  fun toResidentialDetails() =
    ResidentialDetails(
      topLevelLocationType = this.topLevelLocationType,
      subLocationName = this.subLocationName,
      parentLocation = this.parentLocation?.toLocation(),
      subLocations = this.subLocations.map { it.toLocation() },
    )
}
