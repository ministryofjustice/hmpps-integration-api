package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ResidentialHierarchyItem

data class LIPResidentialHierarchyItem(
  val locationId: String,
  val locationType: String,
  val locationCode: String,
  val fullLocationPath: String,
  val localName: String?,
  val level: Int,
  val status: String,
  val subLocations: List<LIPResidentialHierarchyItem>?,
) {
  fun toResidentialHierarchyItem(): ResidentialHierarchyItem =
    ResidentialHierarchyItem(
      locationId = this.locationId,
      locationType = this.locationType,
      locationCode = this.locationCode,
      fullLocationPath = this.fullLocationPath,
      localName = this.localName,
      level = this.level,
      subLocations = this.subLocations?.map { it.toResidentialHierarchyItem() },
    )
}
