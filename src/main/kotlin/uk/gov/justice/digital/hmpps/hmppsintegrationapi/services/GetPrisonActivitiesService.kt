package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RunningActivity
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetPrisonActivitiesService(
  @Autowired val activitiesGateway: ActivitiesGateway,
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
) {
  fun execute(
      prisonId: String,
      filters: ConsumerFilters?,
  ): Response<List<RunningActivity>?> {
    val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<List<RunningActivity>>(prisonId, filters, upstreamServiceType = UpstreamApi.ACTIVITIES)
    if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
      return consumerPrisonFilterCheck
    }

    val runningActivitiesResponse = activitiesGateway.getAllRunningActivities(prisonId)

    return Response(
      data = runningActivitiesResponse.data?.map { it.toRunningActivity() },
      errors = runningActivitiesResponse.errors,
    )
  }
}
