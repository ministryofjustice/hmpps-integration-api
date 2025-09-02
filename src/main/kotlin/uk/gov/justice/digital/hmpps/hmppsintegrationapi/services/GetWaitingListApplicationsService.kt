package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedWaitingListApplications
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.WaitingListSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleFilters

@Service
class GetWaitingListApplicationsService(
  @Autowired private val activitiesGateway: ActivitiesGateway,
  @Autowired private val consumerPrisonAccessService: ConsumerPrisonAccessService,
) {
  fun execute(
    prisonId: String,
    waitingListSearchRequest: WaitingListSearchRequest,
    filters: RoleFilters?,
    page: Int = 1,
    perPage: Int = 50,
  ): Response<PaginatedWaitingListApplications?> {
    val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<PaginatedWaitingListApplications>(prisonId, filters, UpstreamApi.ACTIVITIES)
    if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
      return consumerPrisonFilterCheck
    }

    val waitingListApplicationsResponse =
      activitiesGateway.getWaitingListApplications(
        prisonCode = prisonId,
        activitiesWaitingListSearchRequest = waitingListSearchRequest.toActivitiesWaitingListSearchRequest(),
        page,
        pageSize = perPage,
      )
    if (waitingListApplicationsResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = waitingListApplicationsResponse.errors)
    }

    return Response(
      data = waitingListApplicationsResponse.data?.toPaginatedWaitingListApplications(),
    )
  }
}
