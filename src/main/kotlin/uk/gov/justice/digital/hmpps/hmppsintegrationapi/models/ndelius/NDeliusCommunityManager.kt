package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonResponsibleOfficerName
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonResponsibleOfficerTeam

data class NDeliusCommunityManager(
  val code: String? = null,
  val name: NDeliusName = NDeliusName(),
  val username: String? = null,
  val email: String? = null,
  val telephoneNumber: String? = null,
  val team: NDeliusTeam = NDeliusTeam(),
  val allocated: Boolean? = null,
) {
  fun toCommunityOffenderManager(): CommunityOffenderManager =
    (
      CommunityOffenderManager(
        name = PersonResponsibleOfficerName(forename = this.name.forename, surname = this.name.surname),
        email = this.email,
        telephoneNumber = this.telephoneNumber,
        team = PersonResponsibleOfficerTeam(code = this.team.code, description = this.team.description, email = this.team.email, telephoneNumber = this.team.telephoneNumber),
      )
    )
}
