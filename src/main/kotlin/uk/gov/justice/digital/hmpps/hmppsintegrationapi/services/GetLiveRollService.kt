package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedLiveRoll
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetLiveRollService(
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
) {
  fun execute(
    prisonId: String,
    page: Int,
    size: Int,
    requestContext: RequestContext? = null,
  ): Response<PaginatedLiveRoll?> {
    val response = prisonerOffenderSearchGateway.getPersonsFromPrisonId(prisonId, page, size, requestContext)

    if (response.data?.content?.size == 0) {
      throw EntityNotFoundException("Prison ID $prisonId did not return any persons")
    }

    return Response(data = response.data?.toPaginatedLiveRoll(), errors = response.errors)
  }
}
