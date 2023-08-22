package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Alert
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response

@Service
class GetAlertsForPersonService(
  @Autowired val nomisGateway: NomisGateway,
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
) {
  fun execute(pncId: String): Response<List<Alert>> {
    val responseFromPrisonerOffenderSearch = prisonerOffenderSearchGateway.getPersons(pncId = pncId)
    val nomisNumber = responseFromPrisonerOffenderSearch.data.firstOrNull()?.identifiers?.nomisNumber
    var nomisAlerts: Response<List<Alert>> = Response(data = emptyList())

    if (nomisNumber != null) {
      nomisAlerts = nomisGateway.getAlertsForPerson(nomisNumber)
    }

    return Response(
      data = nomisAlerts.data,
      errors = nomisAlerts.errors + responseFromPrisonerOffenderSearch.errors,
    )
  }
}
