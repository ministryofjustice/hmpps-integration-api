package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonVisitsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.PaginatedVisit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetVisitsService(
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
  @Autowired val prisonVisitsGateway: PrisonVisitsGateway,
) {
  fun execute(
    hmppsId: String? = null,
    prisonId: String,
    fromDate: String?,
    toDate: String?,
    visitStatus: String,
    page: Int,
    size: Int,
    filters: ConsumerFilters? = null,
  ): Response<PaginatedVisit?> {
    val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<PaginatedVisit?>(prisonId, filters, upstreamServiceType = UpstreamApi.MANAGE_PRISON_VISITS)

    if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
      return consumerPrisonFilterCheck
    }

    if (visitStatus !in listOf("BOOKED", "CANCELLED")) {
      return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.MANAGE_PRISON_VISITS, UpstreamApiError.Type.BAD_REQUEST, "Invalid visit status")))
    }

    val response = prisonVisitsGateway.getVisits(prisonId, hmppsId, fromDate, toDate, visitStatus, page, size)

    return response
  }
}
