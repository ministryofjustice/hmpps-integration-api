package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig

data class ConsumerFilters(
  val prisons: List<String>?,
  val caseNotes: List<String>? = null,
) {
  fun matchesPrison(prisonId: String?): Boolean = matchesFilterList(prisons, prisonId)

  private fun matchesFilterList(
    filterList: List<String>?,
    value: String?,
  ): Boolean {
    if (filterList == null) {
      return true
    }
    if (filterList.contains("*")) {
      return true
    }
    if (filterList.contains(value)) {
      return true
    }
    return false
  }

  fun hasPrisonFilter(): Boolean = this.prisons != null
}
