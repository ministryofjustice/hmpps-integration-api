package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class PersonResponsibleOfficer(
  val prisonOfficerManager: PrisonOfficerManager = PrisonOfficerManager(),
  val communityOffenderManager: CommunityOffenderManager = CommunityOffenderManager(),
)
