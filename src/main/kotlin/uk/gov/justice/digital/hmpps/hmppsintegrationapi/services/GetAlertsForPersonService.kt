package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Alert
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

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

  fun getAlertsForPnd(hmppsId: String): Response<List<Alert>> {
    val personResponse = getPersonService.execute(hmppsId = hmppsId)
    val nomisNumber = personResponse.data?.identifiers?.nomisNumber
    var nomisAlerts: Response<List<Alert>> = Response(data = emptyList())

    if (nomisNumber != null) {
      val allNomisAlerts = nomisGateway.getAlertsForPerson(nomisNumber)
      val filteredAlerts =
        allNomisAlerts.data?.filter {
          it.code in
            listOf(
              "BECTER", "HA", "XA", "XCA", "XEL", "XELH", "XER", "XHT", "XILLENT",
              "XIS", "XR", "XRF", "XSA", "HA2", "RCS", "RDV", "RKC", "RPB", "RPC",
              "RSS", "RST", "RDP", "REG", "RLG", "ROP", "RRV", "RTP", "RYP", "HS", "SC",
            )
        }.orEmpty()
      if (filteredAlerts.isEmpty()) {
        return Response(
          data = emptyList(),
          errors =
            listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            ),
        )
      }
      nomisAlerts = Response(data = filteredAlerts, errors = allNomisAlerts.errors)
    }

    return Response(
      data = nomisAlerts.data,
      errors = nomisAlerts.errors + personResponse.errors,
    )
  }
}
