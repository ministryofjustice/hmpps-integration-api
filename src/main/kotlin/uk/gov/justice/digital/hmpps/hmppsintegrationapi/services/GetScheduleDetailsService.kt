package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivityScheduleDetailed
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleFilters

@Service
class GetScheduleDetailsService(
  @Autowired private val activitiesGateway: ActivitiesGateway,
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
) {
  fun execute(
    scheduleId: Long,
    filters: RoleFilters?,
  ): Response<ActivityScheduleDetailed?> {
    val scheduleDetailsResponse = activitiesGateway.getActivityScheduleById(scheduleId)
    if (scheduleDetailsResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = scheduleDetailsResponse.errors)
    }

    val prisonCode =
      scheduleDetailsResponse.data
        ?.activity
        ?.prisonCode
    val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<ActivityScheduleDetailed>(prisonCode, filters, upstreamServiceType = UpstreamApi.ACTIVITIES)
    if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
      return consumerPrisonFilterCheck
    }

    return Response(
      data = scheduleDetailsResponse.data?.toActivityScheduleDetailed(),
      errors = scheduleDetailsResponse.errors,
    )
  }
}
