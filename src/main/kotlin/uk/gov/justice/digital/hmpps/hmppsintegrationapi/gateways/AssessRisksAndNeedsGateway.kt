package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.FeatureNotEnabledException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.ArnNeeds
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.ArnRiskPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.ArnRisks
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Needs
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Risks
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi

@Component
class AssessRisksAndNeedsGateway(
  @Value("\${services.assess-risks-and-needs.base-url}") baseUrl: String,
  private val featureConfig: FeatureFlagConfig,
) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getRiskPredictorScoresForPerson(id: String): Response<List<RiskPredictorScore>> {
    if (!featureConfig.useArnsEndpoints) throw FeatureNotEnabledException(FeatureFlagConfig.USE_ARNS_ENDPOINTS)
    val result =
      webClient.requestList<ArnRiskPredictorScore>(
        HttpMethod.GET,
        "/risks/predictors/$id",
        authenticationHeader(),
        UpstreamApi.ASSESS_RISKS_AND_NEEDS,
        forbiddenAsError = true,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(
          data =
            result.data
              .map { it.toRiskPredictorScore() }
              .sortedByDescending { it.completedDate },
        )
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = emptyList(),
          errors = result.errors,
        )
      }
    }
  }

  fun getRiskSeriousHarmForPerson(id: String): Response<Risks?> {
    if (!featureConfig.useArnsEndpoints) throw FeatureNotEnabledException(FeatureFlagConfig.USE_ARNS_ENDPOINTS)
    val result =
      webClient.request<ArnRisks>(
        HttpMethod.GET,
        "/risks/rosh/$id",
        authenticationHeader(),
        UpstreamApi.ASSESS_RISKS_AND_NEEDS,
        forbiddenAsError = true,
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

  fun getNeedsForPerson(id: String): Response<Needs?> {
    if (!featureConfig.useArnsEndpoints) throw FeatureNotEnabledException(FeatureFlagConfig.USE_ARNS_ENDPOINTS)
    val result =
      webClient.request<ArnNeeds>(
        HttpMethod.GET,
        "/needs/$id",
        authenticationHeader(),
        UpstreamApi.ASSESS_RISKS_AND_NEEDS,
        forbiddenAsError = true,
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
