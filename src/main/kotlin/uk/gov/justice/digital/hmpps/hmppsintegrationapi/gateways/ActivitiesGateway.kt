package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivitySchedule
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivityScheduleDetailed
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesPrisonRegime
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

  fun getActivitiesSchedule(activityId: Long): Response<List<ActivitiesActivitySchedule>?> {
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
}
