package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.MappaCategory

data class ConsumerFilters(
  val prisons: List<String>? = null,
  val caseNotes: List<String>? = null,
  val mappaCategories: List<Any>? = null,
  val alertCodes: List<String>? = null,
  val supervisionStatus: SupervisionStatus = SupervisionStatus.NONE,
) {
  companion object {
    fun mappaCategories(filters: ConsumerFilters?): List<Number> =
      if (filters?.hasMappaCategoriesFilter() == true) {
        // Filters are applied even if empty list
        filters.mappaCategories!!.filterIsInstance<MappaCategory>().mapNotNull {
          it.category
        }
      } else {
        // No filters to be applied - all applicable
        MappaCategory.all().mapNotNull { it.category }
      }

    fun alertCodes(filters: ConsumerFilters?) = filters?.alertCodes ?: emptyList()
  }

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

  fun hasPrisonFilter(): Boolean = this.prisons != null

  fun hasCaseNotesFilter(): Boolean = this.caseNotes != null

  fun hasMappaCategoriesFilter(): Boolean = this.mappaCategories != null

  fun hasAlertCodes(): Boolean = this.alertCodes != null

  fun hasFilters(): Boolean = hasPrisonFilter() || hasCaseNotesFilter() || hasMappaCategoriesFilter() || hasAlertCodes()
}

enum class SupervisionStatus {
  PRISON,
  PROBATION,
  NONE,
}
