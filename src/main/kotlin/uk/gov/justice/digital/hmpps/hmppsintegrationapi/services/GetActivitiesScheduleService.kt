package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivitySchedule
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetActivitiesScheduleService(
  @Autowired val activitiesGateway: ActivitiesGateway,
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
) {
  fun execute(
      activityId: Long,
      filters: ConsumerFilters?,
  ): Response<List<ActivitySchedule>?> {
    val activitiesScheduleResponse = activitiesGateway.getActivitySchedules(activityId)
    if (activitiesScheduleResponse.errors.isNotEmpty()) {
      return Response(
        data = null,
        errors = activitiesScheduleResponse.errors,
      )
    }

    val prisonCode =
      activitiesScheduleResponse.data
        ?.get(0)
        ?.activity
        ?.prisonCode
    val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<List<ActivitySchedule>>(prisonCode, filters, upstreamServiceType = UpstreamApi.ACTIVITIES)
    if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
      return consumerPrisonFilterCheck
    }

    return Response(
      data = activitiesScheduleResponse.data?.map { it.toActivitySchedule() },
      errors = activitiesScheduleResponse.errors,
    )
  }
}
