package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiPrisonTimeline
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.convertDatesToInstant

@Service
class GetPrisonTimelineForPersonService(
  @Autowired val getPersonService: GetPersonService,
  @Autowired val prisonApiGateway: PrisonApiGateway,
) {
  fun getPrisonTimeline(
    hmppsId: String,
    requestContext: RequestContext?,
  ): Response<PrisonApiPrisonTimeline?> {
    val personResponse = getPersonService.getNomisNumber(hmppsId, requestContext?.filters)

    if (personResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = personResponse.errors)
    }

    val nomisNumber =
      personResponse.data?.nomisNumber ?: return Response(
        data = null,
        errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND)),
      )

    val prisonTimelineResponse = prisonApiGateway.getPrisonTimelineForPerson(nomisNumber, requestContext)
    if (prisonTimelineResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = prisonTimelineResponse.errors)
    }

    return Response(
      data = prisonTimelineResponse.data?.convertDatesToInstant(),
    )
  }
}
