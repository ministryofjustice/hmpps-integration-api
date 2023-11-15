package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Alert
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response

@Service
class GetAlertsForPersonService(
  @Autowired val nomisGateway: NomisGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(hmppsId: String): Response<List<Alert>> {
    val personResponse = getPersonService.execute(hmppsId = hmppsId)
    val nomisNumber = personResponse.data?.identifiers?.nomisNumber
    var nomisAlerts: Response<List<Alert>> = Response(data = emptyList())

    if (nomisNumber != null) {
      nomisAlerts = nomisGateway.getAlertsForPerson(nomisNumber)
    }

    return Response(
      data = nomisAlerts.data,
      errors = nomisAlerts.errors + personResponse.errors,
    )
  }
}
