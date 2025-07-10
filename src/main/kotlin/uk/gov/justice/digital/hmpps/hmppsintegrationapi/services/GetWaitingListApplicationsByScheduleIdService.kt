package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.WaitingListApplication
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetWaitingListApplicationsByScheduleIdService(
  @Autowired val activitiesGateway: ActivitiesGateway,
  @Autowired val getScheduleDetailsService: GetScheduleDetailsService,
) {
  fun execute(
    scheduleId: Long,
    filters: ConsumerFilters?,
  ): Response<List<WaitingListApplication>?> {
    val checkPrisonCode = getScheduleDetailsService.execute(scheduleId, filters)
    if (checkPrisonCode.errors.isNotEmpty()) {
      return Response(data = null, errors = checkPrisonCode.errors)
    }

    val waitingListApplicationResponse = activitiesGateway.getWaitingListApplicationsByScheduleId(scheduleId)
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
