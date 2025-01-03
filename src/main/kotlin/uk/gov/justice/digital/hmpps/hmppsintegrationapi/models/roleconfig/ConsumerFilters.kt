package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig

data class ConsumerFilters(
  val filters: Map<String, List<String>>? = emptyMap(),
) {
  fun matchesAtKey(
    filterKey: String,
    value: String?,
  ): Boolean {
    if (filters == null) {
      return true
    }
    if (!filters.containsKey(filterKey)) {
      return true
    }
    if (filters.getOrDefault(filterKey, emptyList()).contains(value)) {
      return true
    }
    return false
  }
}
