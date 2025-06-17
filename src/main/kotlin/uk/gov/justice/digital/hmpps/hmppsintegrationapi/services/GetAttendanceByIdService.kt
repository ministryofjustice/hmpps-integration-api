package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Attendance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetAttendanceByIdService(
  @Autowired val activitiesGateway: ActivitiesGateway,
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
  private val getPersonService: GetPersonService,
) {
  fun execute(
    attendanceId: Long,
    filters: ConsumerFilters?,
  ): Response<Attendance?> {
    val attendanceResponse = activitiesGateway.getAttendanceById(attendanceId)
    if (attendanceResponse.errors.isNotEmpty()) {
      return Response(
        data = null,
        errors = attendanceResponse.errors,
      )
    }

    val hmppsId =
      attendanceResponse.data?.prisonerNumber
        ?: return Response(
          data = null,
          errors = listOf(UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.ENTITY_NOT_FOUND)),
        )
    val personResponse = getPersonService.getPersonWithPrisonFilter(hmppsId, filters)
    if (personResponse.errors.isNotEmpty()) {
      return Response(
        data = null,
        errors = personResponse.errors,
      )
    }

    return Response(
      data = attendanceResponse.data.toAttendance(),
    )
  }
}
