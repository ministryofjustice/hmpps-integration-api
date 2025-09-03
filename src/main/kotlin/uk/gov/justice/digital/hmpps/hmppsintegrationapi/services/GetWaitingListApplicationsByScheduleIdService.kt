package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.WaitingListApplication
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetWaitingListApplicationsByScheduleIdService(
  @Autowired val activitiesGateway: ActivitiesGateway,
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
) {
  fun execute(
      scheduleId: Long,
      filters: ConsumerFilters?,
  ): Response<List<WaitingListApplication>?> {
    val scheduleDetailsResponse = activitiesGateway.getActivityScheduleById(scheduleId)
    if (scheduleDetailsResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = scheduleDetailsResponse.errors)
    }

    val prisonCode =
      scheduleDetailsResponse.data
        ?.activity
        ?.prisonCode!!
    val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<List<WaitingListApplication>>(prisonCode, filters, upstreamServiceType = UpstreamApi.ACTIVITIES)
    if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
      return consumerPrisonFilterCheck
    }

    val waitingListApplicationResponse = activitiesGateway.getWaitingListApplicationsByScheduleId(scheduleId, prisonCode)
    if (waitingListApplicationResponse.errors.isNotEmpty()) {
      return Response(
        data = null,
        errors = waitingListApplicationResponse.errors,
      )
    }

    return Response(
      data = waitingListApplicationResponse.data?.map { it.toWaitingListApplication() },
    )
  }
}
