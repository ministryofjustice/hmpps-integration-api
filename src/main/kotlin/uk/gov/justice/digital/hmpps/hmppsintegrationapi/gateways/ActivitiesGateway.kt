package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivitySchedule
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivityScheduleDetailed
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivityScheduledInstanceForPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesAppointmentDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesAttendance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesAttendanceReason
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesDeallocationReason
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesHistoricalAttendance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesPagedWaitingListApplication
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesPrisonPayBand
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesPrisonRegime
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesRunningActivity
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesSuitabilityCriteria
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesWaitingListApplication
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesWaitingListSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AppointmentSearchRequest
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

  fun getActivitySuitabilityCriteria(scheduleId: Long): Response<ActivitiesSuitabilityCriteria?> {
    val result =
      webClient.request<ActivitiesSuitabilityCriteria>(
        HttpMethod.GET,
        "/integration-api/activities/schedule/$scheduleId/suitability-criteria",
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
    appointmentSearchRequest: AppointmentSearchRequest,
  ): Response<List<ActivitiesAppointmentDetails>?> {
    val result =
      webClient.requestList<ActivitiesAppointmentDetails>(
        HttpMethod.POST,
        "/appointments/$prisonCode/search",
        authenticationHeader(),
        requestBody = appointmentSearchRequest.toApiConformingMap(),
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
    prisonerNumber: String,
    startDate: String,
    endDate: String,
    slot: String?,
  ): Response<List<ActivitiesActivityScheduledInstanceForPrisoner>?> {
    val queryParams =
      buildList {
        add("startDate=$startDate")
        add("endDate=$endDate")
        slot?.let { add("slot=$it") }
      }.joinToString("&")

    val result =
      webClient.requestList<ActivitiesActivityScheduledInstanceForPrisoner>(
        method = HttpMethod.GET,
        uri = "/integration-api/prisons/$prisonCode/$prisonerNumber/scheduled-instances?$queryParams",
        headers = authenticationHeader(),
        upstreamApi = UpstreamApi.ACTIVITIES,
        badRequestAsError = true,
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

  fun getDeallocationReasons(): Response<List<ActivitiesDeallocationReason>?> {
    val result =
      webClient.requestList<ActivitiesDeallocationReason>(
        HttpMethod.GET,
        "/integration-api/allocations/deallocation-reasons",
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

  fun getWaitingListApplications(
    prisonCode: String,
    activitiesWaitingListSearchRequest: ActivitiesWaitingListSearchRequest,
    page: Int = 1,
    pageSize: Int = 50,
  ): Response<ActivitiesPagedWaitingListApplication?> {
    val result =
      webClient.request<ActivitiesPagedWaitingListApplication>(
        HttpMethod.POST,
        "/waiting-list-applications/$prisonCode/search?page=${page - 1}&pageSize=$pageSize",
        authenticationHeader(),
        UpstreamApi.ACTIVITIES,
        requestBody = activitiesWaitingListSearchRequest.toApiConformingMap(),
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

  fun getHistoricalAttendances(
    prisonerNumber: String,
    startDate: String,
    endDate: String,
    prisonCode: String?,
  ): Response<List<ActivitiesHistoricalAttendance>?> {
    val prisonCodeParam = prisonCode?.let { "&prisonCode=$it" } ?: ""
    val result =
      webClient.requestList<ActivitiesHistoricalAttendance>(
        HttpMethod.GET,
        "/integration-api/attendances/prisoner/$prisonerNumber?startDate=$startDate&endDate=$endDate$prisonCodeParam",
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

  fun getWaitingListApplicationsByScheduleId(
    scheduleId: Long,
    prisonCode: String,
  ): Response<List<ActivitiesWaitingListApplication>?> {
    val result =
      webClient.requestList<ActivitiesWaitingListApplication>(
        HttpMethod.GET,
        "/integration-api/schedules/$scheduleId/waiting-list-applications",
        authenticationHeader() + mapOf("Caseload-Id" to prisonCode),
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
