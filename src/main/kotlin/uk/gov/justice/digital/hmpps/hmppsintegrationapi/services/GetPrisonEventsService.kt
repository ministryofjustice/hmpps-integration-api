package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiEvents
import java.time.LocalDateTime

@Service
class GetPrisonEventsService(
  @Autowired val prisonApiGateway: PrisonApiGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(
    prisonId: String,
    hmppsId: String?,
    eventType: String?,
    fromDateTime: LocalDateTime,
    limit: Int?,
    requestContext: RequestContext,
  ): Response<PrisonApiEvents?> {
    val nomisNumber = getNomisNumber(hmppsId)

    if (hmppsId != null && nomisNumber == null) {
      return Response(
        data = null,
        errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND)),
      )
    }

    val response = prisonApiGateway.getEvents(prisonId, nomisNumber, eventType, fromDateTime, limit, requestContext)

    return Response(data = response.data?.convertTimestamps(), errors = response.errors)
  }

  private fun getNomisNumber(hmppsId: String?): String? {
    if (hmppsId == null) return null
    val personResponse = getPersonService.getNomisNumber(hmppsId)
    if (personResponse.errors.isNotEmpty()) {
      return null
    }
    return personResponse.data?.nomisNumber
  }
}
