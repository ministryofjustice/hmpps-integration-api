package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.oboconfig

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.manageUsers.HmppsAuthUsernames

data class OboUser(
  val username: String,
  val hasPrisonRole: Boolean? = null,
  val authUsernames: HmppsAuthUsernames? = null,
) {
  fun excludedSourceSystems(): List<String> {
    val list = mutableListOf<String>()
    if (hasPrisonRole != null && !hasPrisonRole) {
      list.add("NOMIS")
    }
    if (authUsernames != null && authUsernames.deliusUsername == null) {
      list.add("DELIUS")
    }
    return list
  }
}
