package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.manageUsers

data class HmppsPrisonUser(
  val username: String,
  val activeCaseloadId: String? = null,
  val dpsRoleCodes: List<String> = emptyList(),
  val enabled: Boolean = true,
)
