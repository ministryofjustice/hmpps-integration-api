package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitOrders
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetVisitOrdersForPersonService(
  @Autowired val prisonApiGateway: PrisonApiGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(
      hmppsId: String,
      filters: ConsumerFilters? = null,
  ): Response<VisitOrders?> {
    val personResponse = getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)
    if (personResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = personResponse.errors)
    }

    val nomisNumber =
      personResponse.data?.nomisNumber ?: return Response(
        data = null,
        errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)),
      )

    val visitBalancesResponse = prisonApiGateway.getVisitBalances(nomisNumber)
    if (visitBalancesResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = visitBalancesResponse.errors)
    }

    val transformedResponse: Response<VisitOrders?> =
      Response(
        data =
          VisitOrders(
            remainingVisitOrders = visitBalancesResponse.data?.remainingVo ?: 0L,
            remainingPrivilegeVisitOrders = visitBalancesResponse.data?.remainingPvo ?: 0L,
          ),
      )

    return transformedResponse
  }
}
