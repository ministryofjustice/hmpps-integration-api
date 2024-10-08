package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class PersonResponsibleOfficer(
  val prisonOffenderManager: PrisonOffenderManager = PrisonOffenderManager(),
  val communityOffenderManager: CommunityOffenderManager = CommunityOffenderManager(),
)
