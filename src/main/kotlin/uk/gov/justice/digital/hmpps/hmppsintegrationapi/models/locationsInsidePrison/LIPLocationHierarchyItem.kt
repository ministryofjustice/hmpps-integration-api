package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison

data class LIPLocationHierarchyItem(
  val id: String?,
  val prisonId: String,
  val code: String,
  val type: String,
  val localName: String?,
  val pathHierarchy: String,
  val level: Int,
)
