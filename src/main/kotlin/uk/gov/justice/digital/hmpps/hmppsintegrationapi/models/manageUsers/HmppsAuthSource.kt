package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.manageUsers

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.MappaCategory.entries

enum class HmppsAuthSource(
  val value: String,
) {
  NDELIUS("delius"),
  AZUREAD("azuread"),
  NOMIS("nomis"),
  ;

  companion object {
    fun of(value: String): HmppsAuthSource = HmppsAuthSource.entries.filter { it.value == value }.firstOrNull() ?: throw RuntimeException("AuthSource not found")
  }
}
