package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.manageUsers

data class HmppsPrisonUserCaseload(
  val username: String,
  val active: Boolean,
  val caseloads: List<HmppsPrisonCaseload> = emptyList(),
)
