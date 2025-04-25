package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison

data class LIPResidentialHierarchyItem(
  val locationId: String,
  val locationType: String,
  val locationCode: String,
  val fullLocationPath: String,
  val localName: String?,
  val level: Int,
  val subLocations: List<String>?,
)
