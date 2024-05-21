package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.CrnRiskManagementPlans
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi

@Component
class RiskManagementGateway(
  @Value("\${services.assess-risks-and-needs.base-url}") baseUrl: String,
) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getRiskManagementPlansForCrn(crn: String): Response<CrnRiskManagementPlans?> {
    val result =
      webClient.request<CrnRiskManagementPlans>(
        HttpMethod.GET,
        "/risks/crn/$crn/risk-management-plan",
        authenticationHeader(),
        UpstreamApi.RISK_MANAGEMENT_PLAN,
      )
    return when (result) {
      is WebClientWrapper.WebClientWrapperResponse.Success -> {
        Response(data = result.data, errors = emptyList())
      }

      is WebClientWrapper.WebClientWrapperResponse.Error -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("Risk Management Plan Search")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
