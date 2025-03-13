package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonerAlerts.PAPaginatedAlerts

@Component
class PrisonerAlertsGateway(
  @Value("\${services.alerts.base-url}") baseUrl: String,
) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("PRISONER_ALERTS")
    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }

  fun getPrisonerAlerts(
    prisonerNumber: String,
    page: Int,
    size: Int,
  ): Response<PAPaginatedAlerts?> {
    val result =
      webClient.request<PAPaginatedAlerts>(
        HttpMethod.GET,
        "/prisoners/$prisonerNumber/alerts?isActive=true&page=$page&size=$size",
        authenticationHeader(),
        UpstreamApi.PRISONER_ALERTS,
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
