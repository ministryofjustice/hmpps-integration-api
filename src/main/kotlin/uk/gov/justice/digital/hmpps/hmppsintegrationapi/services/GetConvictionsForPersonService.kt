package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Conviction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response

@Service
class GetConvictionsForPersonService(
  @Autowired val nomisGateway: NomisGateway,
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
) {
  fun execute(pncId: String): Response<List<Conviction>> {
    val responseFromPrisonerOffenderSearch = prisonerOffenderSearchGateway.getPersons(pncId = pncId)

    return nomisGateway.getConvictionsForPerson(responseFromPrisonerOffenderSearch.data.first().prisonerId!!)
  }
}
