package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskPredictor as IntegrationAPIRiskPredictor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.RiskPredictors as ArnRiskPredictors

@Component
class AssessRisksAndNeedsGateway(@Value("\${services.assess-risks-and-needs.base-url}") baseUrl: String) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getRiskPredictorsForPerson(id: String): Response<List<IntegrationAPIRiskPredictor>> {
    return try {
      Response(
        data = webClient.request<ArnRiskPredictors>(
          HttpMethod.GET,
          "/risks/crn/$id/predictors/all",
          authenticationHeader(),
        ).arnRiskPredictors.map { it.toRiskPredictor() },
      )
    } catch (exception: WebClientResponseException.NotFound) {
      Response(
        data = emptyList(),
        errors = listOf(
          UpstreamApiError(
            causedBy = UpstreamApi.ARN,
            type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
          ),
        ),
      )
    }
  }

  private fun authenticationHeader(): Map<String, String> {
    // TODO we don't know what the token will be yet
    val token = hmppsAuthGateway.getClientToken("ARN")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
