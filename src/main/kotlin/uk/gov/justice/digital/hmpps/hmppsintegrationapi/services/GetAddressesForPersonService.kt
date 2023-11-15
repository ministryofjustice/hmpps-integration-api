package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response

@Service
class GetAddressesForPersonService(
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @Autowired val nomisGateway: NomisGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(pncId: String): Response<List<Address>> {
    val responseFromPrisonerOffenderSearch = prisonerOffenderSearchGateway.getPersons(pncId = pncId)

    val responseFromNomis = nomisGateway.getAddressesForPerson(responseFromPrisonerOffenderSearch.data.first().identifiers.nomisNumber!!)
    val responseFromProbationOffenderSearch = getPersonService.getAddressesForPerson(hmppsId = pncId)

    return Response.merge(listOf(responseFromNomis, responseFromProbationOffenderSearch))
  }
}
