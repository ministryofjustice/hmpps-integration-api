package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiMovementsResponse

@Service
class GetMovementService(
  @Autowired val getPersonService: GetPersonService,
  @Autowired val prisonApiGateway: PrisonApiGateway,
) {
  fun getMovement(
    hmppsId: String,
    requestContext: RequestContext?,
  ): Response<PrisonApiMovementsResponse?> {
    val personResponse = getPersonService.getNomisNumber(hmppsId, requestContext?.filters)

    if (personResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = personResponse.errors)
    }

    val nomisNumber =
      personResponse.data?.nomisNumber ?: return Response(
        data = null,
        errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND)),
      )

    val movementsResponse = prisonApiGateway.getMovementsForPerson(nomisNumber, requestContext)
    if (movementsResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = movementsResponse.errors)
    }

    return Response(
      data = movementsResponse.data?.toResponse(),
    )
  }
}
