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
) : UpstreamGateway {
  override fun metaData() =
    GatewayMetadata(
      summary = "Move prisoner alerts data out of NOMIS",
      developerPortalId = "DPS090",
      developerPortalUrl = "https://developer-portal.hmpps.service.justice.gov.uk/components/hmpps-alerts-api",
      apiDocUrl = "https://alerts-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html",
      apiSpecUrl = "https://alerts-api-dev.hmpps.service.justice.gov.uk/v3/api-docs",
      gitHubRepoUrl = "https://github.com/ministryofjustice/hmpps-alerts-api",
    )

  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("PRISONER_ALERTS")
    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }

  /**
   * @param page page number (1 based)
   * @param size records per page
   * @param alertCodes The alert codes to return in the response. If empty then all codes will be returned
   */
  fun getPrisonerAlertsForCodes(
    prisonerNumber: String,
    page: Int,
    size: Int,
    alertCodes: List<String> = emptyList(),
  ): Response<PAPaginatedAlerts?> {
    val uri = "/prisoners/$prisonerNumber/alerts?page=${page - 1}&size=$size"
    val result =
      webClient.request<PAPaginatedAlerts>(
        HttpMethod.GET,
        if (alertCodes.isNotEmpty()) {
          "$uri&alertCode=${alertCodes.joinToString(",")}"
        } else {
          uri
        },
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
