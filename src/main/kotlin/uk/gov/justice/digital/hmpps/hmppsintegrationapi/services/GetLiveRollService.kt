package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiLiveRoll

@Service
class GetLiveRollService(
  @Autowired val prisonApiGateway: PrisonApiGateway,
) {
  fun execute(
    prisonId: String,
    requestContext: RequestContext,
  ): Response<PrisonApiLiveRoll?> {
    val response = prisonApiGateway.getLiveRoll(prisonId, requestContext)

    return Response(data = response.data, errors = response.errors)
  }
}
