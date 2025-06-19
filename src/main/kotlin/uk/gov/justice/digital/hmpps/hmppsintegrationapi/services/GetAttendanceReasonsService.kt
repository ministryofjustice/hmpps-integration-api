package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ReasonForAttendance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetAttendanceReasonsService(
  @Autowired val activitiesGateway: ActivitiesGateway,
) {
  fun execute(): Response<List<ReasonForAttendance>?> {
    val attendanceResponse = activitiesGateway.getAttendanceReasons()
    if (attendanceResponse.errors.isNotEmpty()) {
      return Response(
        data = null,
        errors = attendanceResponse.errors,
      )
    }

    return Response(
      data = attendanceResponse.data?.map { it.toReasonForAttendance() },
    )
  }
}
