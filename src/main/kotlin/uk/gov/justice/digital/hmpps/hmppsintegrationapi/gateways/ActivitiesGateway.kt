package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivitySchedule
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivityScheduleDetailed
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivityScheduledInstancesForPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesAppointmentDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesAttendance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesAttendanceReason
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesPrisonPayBand
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesPrisonRegime
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesRunningActivity
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi

@Component
class ActivitiesGateway(
  @Value("\${services.activities.base-url}") baseUrl: String,
) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("ACTIVITIES")
    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }

  fun getPrisonRegime(prisonCode: String): Response<List<ActivitiesPrisonRegime>?> {
    val result =
      webClient.requestList<ActivitiesPrisonRegime>(
        HttpMethod.GET,
        "/prison/prison-regime/$prisonCode",
        authenticationHeader(),
        UpstreamApi.ACTIVITIES,
        badRequestAsError = true,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data)
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }

  fun getPrisonPayBands(prisonCode: String): Response<List<ActivitiesPrisonPayBand>?> {
    val result =
      webClient.requestList<ActivitiesPrisonPayBand>(
        HttpMethod.GET,
        "/prison/$prisonCode/prison-pay-bands",
        authenticationHeader(),
        UpstreamApi.ACTIVITIES,
        badRequestAsError = true,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data)
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }

  fun getActivitySchedules(activityId: Long): Response<List<ActivitiesActivitySchedule>?> {
    val result =
      webClient.requestList<ActivitiesActivitySchedule>(
        HttpMethod.GET,
        "/activities/$activityId/schedules",
        authenticationHeader(),
        UpstreamApi.ACTIVITIES,
        badRequestAsError = true,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data)
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }

  fun getActivityScheduleById(scheduleId: Long): Response<ActivitiesActivityScheduleDetailed?> {
    val result =
      webClient.request<ActivitiesActivityScheduleDetailed>(
        HttpMethod.GET,
        "/schedules/$scheduleId",
        authenticationHeader(),
        UpstreamApi.ACTIVITIES,
        badRequestAsError = true,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data)
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }

  fun getAllRunningActivities(prisonCode: String): Response<List<ActivitiesRunningActivity>?> {
    val result =
      webClient.requestList<ActivitiesRunningActivity>(
        HttpMethod.GET,
        "/prison/$prisonCode/activities",
        authenticationHeader(),
        UpstreamApi.ACTIVITIES,
        badRequestAsError = true,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data)
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }

  fun getAppointments(
    prisonCode: String,
    startDate: String,
  ): Response<List<ActivitiesAppointmentDetails>?> {
    val requestBodyMap =
      mapOf(
        "startDate" to startDate,
      )

    val result =
      webClient.requestList<ActivitiesAppointmentDetails>(
        HttpMethod.POST,
        "/appointments/$prisonCode/search",
        authenticationHeader(),
        requestBody = requestBodyMap,
        upstreamApi = UpstreamApi.ACTIVITIES,
        badRequestAsError = true,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data)
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }

  fun getAttendanceById(attendanceId: Long): Response<ActivitiesAttendance?> {
    val result =
      webClient.request<ActivitiesAttendance>(
        HttpMethod.GET,
        "/attendances/$attendanceId",
        authenticationHeader(),
        UpstreamApi.ACTIVITIES,
        badRequestAsError = true,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data)
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }

  fun getAttendanceReasons(): Response<List<ActivitiesAttendanceReason>?> {
    val result =
      webClient.requestList<ActivitiesAttendanceReason>(
        HttpMethod.GET,
        "/attendance-reasons",
        authenticationHeader(),
        UpstreamApi.ACTIVITIES,
        forbiddenAsError = true,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data)
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }

  fun getScheduledInstancesForPrisoner(
    prisonCode: String,
    prisonerId: String,
    startDate: String,
    endDate: String,
    slot: String?,
    cancelled: Boolean?,
  ): Response<List<ActivitiesActivityScheduledInstancesForPrisoner>?> {
    val result =
      webClient.requestList<ActivitiesActivityScheduledInstancesForPrisoner>(
        HttpMethod.GET,
        "/prisons/$prisonCode/$prisonerId/scheduled-instances",
        authenticationHeader(),
        UpstreamApi.ACTIVITIES,
        badRequestAsError = true,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data)
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }
}
