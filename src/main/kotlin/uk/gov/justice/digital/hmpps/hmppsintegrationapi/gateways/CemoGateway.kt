package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.util.UriUtils
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cemo.CemoOrderCaseSearchResult
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import java.nio.charset.StandardCharsets

@Component
class CemoGateway(
  @Value("\${services.cemo.base-url}") baseUrl: String,
) : UpstreamGateway {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  override fun metaData() =
    GatewayMetadata(
      summary = "The Create an Electronic Monitoring Order API stores and submits electronic monitoring orders.",
      gitHubRepoUrl = "https://github.com/ministryofjustice/hmpps-electronic-monitoring-create-an-order-api",
    )

  fun getOrderByCaseId(caseId: String): Response<CemoOrderCaseSearchResult?> {
    val encodedCaseId = UriUtils.encodePathSegment(caseId, StandardCharsets.UTF_8)
    val result =
      webClient.request<CemoOrderCaseSearchResult>(
        HttpMethod.GET,
        "/api/orders/search/by-case-id/$encodedCaseId",
        authenticationHeader(),
        UpstreamApi.CEMO,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> Response(data = result.data)
      is WebClientWrapperResponse.Error -> Response(data = null, errors = result.errors)
    }
  }

  private fun authenticationHeader(): Map<String, String> =
    mapOf("Authorization" to "Bearer ${hmppsAuthGateway.getClientToken("CEMO")}")
}
