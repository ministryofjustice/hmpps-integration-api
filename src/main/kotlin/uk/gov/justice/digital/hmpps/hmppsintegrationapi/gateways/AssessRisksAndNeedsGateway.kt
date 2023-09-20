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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskPredictorScore as IntegrationAPIRiskPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Risks as IntegrationApiRisk
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.RiskPredictorScore as ARNRiskPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.Risks as ArnRisk

@Component
class AssessRisksAndNeedsGateway(@Value("\${services.assess-risks-and-needs.base-url}") baseUrl: String) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getRiskPredictorScoresForPerson(id: String): Response<List<IntegrationAPIRiskPredictorScore>> {
    return try {
      Response(
        data = webClient.requestList<ARNRiskPredictorScore>(
          HttpMethod.GET,
          "/risks/crn/$id/predictors/all",
          authenticationHeader(),
        ).map { it.toRiskPredictorScore() },
      )
    } catch (exception: WebClientResponseException.NotFound) {
      Response(
        data = emptyList(),
        errors = listOf(
          UpstreamApiError(
            causedBy = UpstreamApi.ASSESS_RISKS_AND_NEEDS,
            type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
          ),
        ),
      )
    }
  }

  fun getRisksForPerson(id: String): Response<IntegrationApiRisk?> {
    return try {
      Response(
        data = webClient.request<ArnRisk>(
          HttpMethod.GET,
          "/risks/crn/$id",
          authenticationHeader(),
        ).toRisks(),
      )
    } catch (exception: WebClientResponseException.NotFound) {
      Response(
        data = null,
        errors = listOf(
          UpstreamApiError(
            causedBy = UpstreamApi.ASSESS_RISKS_AND_NEEDS,
            type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
          ),
        ),
      )
    }
  }

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("ASSESS_RISKS_AND_NEEDS")
    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
