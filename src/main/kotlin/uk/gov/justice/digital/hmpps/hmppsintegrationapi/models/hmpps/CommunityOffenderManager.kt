package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class CommunityOffenderManager(
  val name: PersonResponsibleOfficerName = PersonResponsibleOfficerName(),
  val email: String? = null,
  val telephoneNumber: String? = null,
  val team: PersonResponsibleOfficerTeam = PersonResponsibleOfficerTeam(),
)
