package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.incentives.IncIEPReviewHistory

@Component
class IncentivesGateway(
  @Value("\${services.incentives.base-url}") baseUrl: String,
) : UpstreamGateway {
  override fun metaData() =
    GatewayMetadata(
      summary = "Manage and review the incentives levels of prisons.",
      developerPortalId = "DPS020",
      developerPortalUrl = "https://developer-portal.hmpps.service.justice.gov.uk/components/hmpps-incentives-api",
      apiSpecUrl = "https://incentives-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html",
      gitHubRepoUrl = "https://github.com/ministryofjustice/hmpps-incentives-api",
    )

  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("INCENTIVES")
    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }

  fun getIEPReviewHistory(prisonerNumber: String): Response<IncIEPReviewHistory?> {
    val result =
      webClient.request<IncIEPReviewHistory>(
        HttpMethod.GET,
        "/incentive-reviews/prisoner/$prisonerNumber",
        authenticationHeader(),
        UpstreamApi.INCENTIVES,
        badRequestAsError = true,
      )
    return when (result) {
      is WebClientWrapper.WebClientWrapperResponse.Success -> {
        return Response(
          data = result.data,
        )
      }
      is WebClientWrapper.WebClientWrapperResponse.Error -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }
}
