package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.alerts.AlertsPaginatedAlerts
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi

@Component
class AlertsGateway(
  @Value("\${services.alerts.base-url}") baseUrl: String,
) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("ALERTS")
    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }

  fun getPrisonerAlerts(prisonerNumber: String): Response<AlertsPaginatedAlerts?> {
    val result =
      webClient.request<AlertsPaginatedAlerts>(
        HttpMethod.GET,
        "/prisoners/$prisonerNumber/alerts",
        authenticationHeader(),
        UpstreamApi.INCENTIVES,
        badRequestAsError = true,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        return Response(
          data = result.data,
        )
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
