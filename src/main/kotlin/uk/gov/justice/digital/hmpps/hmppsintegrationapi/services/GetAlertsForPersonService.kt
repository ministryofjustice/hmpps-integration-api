package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerAlertsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedAlerts
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetAlertsForPersonService(
  @Autowired val getPersonService: GetPersonService,
  @Autowired val prisonerAlertsGateway: PrisonerAlertsGateway,
) {
  fun execute(
    hmppsId: String,
    filter: ConsumerFilters?,
    page: Int,
    perPage: Int,
    pndOnly: Boolean = false,
  ): Response<PaginatedAlerts?> {
    val personResponse = getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filter)
    if (personResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = personResponse.errors)
    }

    val nomisNumber =
      personResponse.data?.nomisNumber ?: return Response(
        data = null,
        errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND)),
      )

    val alertsResponse = prisonerAlertsGateway.getPrisonerAlerts(nomisNumber, page, size = perPage)
    if (alertsResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = alertsResponse.errors)
    }

    val paginatedAlerts = alertsResponse.data?.toPaginatedAlerts(pndOnly)

    if (paginatedAlerts?.content.orEmpty().isEmpty()) {
      return Response(
        data = null,
        errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PRISONER_ALERTS,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          ),
      )
    }

    return Response(
      data = paginatedAlerts,
    )
  }
}
