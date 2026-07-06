package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.manageUsers

data class HmppsAuthUser(
  val username: String,
  val source: String,
  val locked: Boolean = false,
  val enabled: Boolean = true,
)
