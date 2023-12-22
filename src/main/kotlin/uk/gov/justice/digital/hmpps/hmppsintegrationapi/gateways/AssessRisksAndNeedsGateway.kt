package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Needs as IntegrationApiNeeds
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskPredictorScore as IntegrationAPIRiskPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Risks as IntegrationApiRisk
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.Needs as ArnNeeds
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.RiskPredictorScore as ARNRiskPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.Risks as ArnRisk

@Component
class AssessRisksAndNeedsGateway(@Value("\${services.assess-risks-and-needs.base-url}") baseUrl: String) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getRiskPredictorScoresForPerson(id: String): Response<List<IntegrationAPIRiskPredictorScore>> {
    val result = webClient.requestList<ARNRiskPredictorScore>(
      HttpMethod.GET,
      "/risks/crn/$id/predictors/all",
      authenticationHeader(),
      UpstreamApi.ASSESS_RISKS_AND_NEEDS,
    )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data.map { it.toRiskPredictorScore() })
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = emptyList(),
          errors = result.errors,
        )
      }
    }
  }

  fun getRisksForPerson(id: String): Response<IntegrationApiRisk?> {
    val result = webClient.request<ArnRisk>(
      HttpMethod.GET,
      "/risks/crn/$id",
      authenticationHeader(),
      UpstreamApi.ASSESS_RISKS_AND_NEEDS,
    )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data.toRisks())
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }

  fun getNeedsForPerson(id: String): Response<IntegrationApiNeeds?> {
    val result = webClient.request<ArnNeeds>(
      HttpMethod.GET,
      "/needs/crn/$id",
      authenticationHeader(),
      UpstreamApi.ASSESS_RISKS_AND_NEEDS,
    )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data.toNeeds())
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("ASSESS_RISKS_AND_NEEDS")
    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
