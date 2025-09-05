package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HistoricalAttendance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleFilters

@Service
class GetHistoricalAttendancesService(
  @Autowired val activitiesGateway: ActivitiesGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(
    prisonerNumber: String,
    startDate: String,
    endDate: String,
    prisonId: String?,
    filters: RoleFilters?,
  ): Response<List<HistoricalAttendance>?> {
    val getPersonServiceResponse = getPersonService.getPersonWithPrisonFilter(prisonerNumber, filters)
    if (getPersonServiceResponse.errors.isNotEmpty()) {
      return Response(
        data = null,
        errors = getPersonServiceResponse.errors,
      )
    }

    val historicalAttendanceResponse = activitiesGateway.getHistoricalAttendances(prisonerNumber, startDate, endDate, prisonId)
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
