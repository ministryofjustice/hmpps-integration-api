package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response

@Service
class GetAddressesForPersonService(
  @Autowired val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @Autowired val nomisGateway: NomisGateway,
) {
  fun execute(pncId: String): Response<List<Address>> {
    val personFromPrisonerOffenderSearch = prisonerOffenderSearchGateway.getPersons(pncId = pncId)

    val responseFromNomis = nomisGateway.getAddressesForPerson(personFromPrisonerOffenderSearch.first().prisonerId!!)
    val responseFromProbationOffenderSearch = probationOffenderSearchGateway.getAddressesForPerson(pncId)

    return Response.merge(listOf(responseFromNomis, responseFromProbationOffenderSearch))
  }
}
