package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HistoricalAttendance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetHistoricalAttendancesService(
  @Autowired val activitiesGateway: ActivitiesGateway,
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
) {
  fun execute(
    prisonerNumber: String,
    startDate: String,
    endDate: String,
    prisonCode: String?,
    filters: ConsumerFilters?,
  ): Response<List<HistoricalAttendance>?> {
    val checkAccess = consumerPrisonAccessService.checkConsumerHasPrisonAccess<Any>(prisonCode, filters)
    if (checkAccess.errors.isNotEmpty()) {
      return Response(data = null, errors = checkAccess.errors)
    }

    val historicalAttendanceResponse = activitiesGateway.getHistoricalAttendances(prisonerNumber, startDate, endDate, prisonCode)
    if (historicalAttendanceResponse.errors.isNotEmpty()) {
      return Response(
        data = null,
        errors = historicalAttendanceResponse.errors,
      )
    }

    return Response(
      data = historicalAttendanceResponse.data?.map { it.toHistoricalAttendance() },
    )
  }
}
