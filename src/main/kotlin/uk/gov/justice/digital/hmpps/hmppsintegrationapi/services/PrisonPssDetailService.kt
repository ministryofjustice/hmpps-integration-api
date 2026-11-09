package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiPssDetail

@Service
class PrisonPssDetailService(
  private val prisonApiGateway: PrisonApiGateway,
  private val getPersonService: GetPersonService,
) {
  fun pssDetail(
    hmppsId: String,
    requestContext: RequestContext,
  ): Response<PrisonApiPssDetail?> {
    val nomisNumberResponse = getPersonService.getNomisNumber(hmppsId, requestContext.filters)
    if (nomisNumberResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = nomisNumberResponse.errors)
    }
    val nomisNumber = nomisNumberResponse.data?.nomisNumber ?: return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND)))
    val response = prisonApiGateway.getPssDetail(nomisNumber, requestContext)
    return Response(data = response.data, errors = response.errors)
  }
}
