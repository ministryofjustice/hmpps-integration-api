package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig

data class ConsumerFilters(
  val prisons: List<String>?,
) {
  fun matchesPrison(prisonId: String?): Boolean = matchesFilterList(prisons, prisonId)

  private fun matchesFilterList(
    filterList: List<String>?,
    value: String?,
  ): Boolean {
    if (filterList == null) {
      return true
    }
    if (filterList.contains(value)) {
      return true
    }
    return false
  }
}
