package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonVisitsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Visit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetVisitInformationByReferenceService(
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
  @Autowired val prisonVisitsGateway: PrisonVisitsGateway,
) {
  fun execute(
    visitReference: String,
    filters: ConsumerFilters? = null,
  ): Response<Visit?> {
    val prisonVisitsResponse = prisonVisitsGateway.getVisitByReference(visitReference)

    if (prisonVisitsResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = prisonVisitsResponse.errors)
    }

    val prisonId = prisonVisitsResponse.data?.prisonId

    if (prisonId.isNullOrBlank()) {
      return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.MANAGE_PRISON_VISITS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "PrisonId not found")))
    }

    val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<Visit>(prisonId, filters, UpstreamApi.MANAGE_PRISON_VISITS)

    if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
      return consumerPrisonFilterCheck
    }

    return Response(data = prisonVisitsResponse.data.toVisit())
  }
}
