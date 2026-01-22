package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_NEW_RISK_SCORE_API
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.ArnNeeds
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.ArnRiskPredictorScore
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.ArnRiskPredictorScoreV2
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
) : UpstreamGateway {
  override fun metaData() =
    GatewayMetadata(
      summary = "Assess Risks and Needs",
      developerPortalId = "SP01",
      developerPortalUrl = "https://developer-portal.hmpps.service.justice.gov.uk/components/hmpps-assess-risks-and-needs-coordinator-api",
      apiDocUrl = "https://assess-risks-and-needs-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html",
      apiSpecUrl = "https://assess-risks-and-needs-dev.hmpps.service.justice.gov.uk/v3/api-docs",
      gitHubRepoUrl = "https://github.com/ministryofjustice/hmpps-assess-risks-and-needs-coordinator-api",
    )

  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getRiskPredictorScoresForPerson(id: String): Response<List<RiskPredictorScore>> {
    if (featureConfig.isEnabled(USE_NEW_RISK_SCORE_API)) {
      return getRiskPredictorScoreV2(id)
    }
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

  private fun getRiskPredictorScoreV2(id: String): Response<List<RiskPredictorScore>> {
    val result =
      webClient.requestList<ArnRiskPredictorScoreV2>(
        HttpMethod.GET,
        "/risks/predictors/unsafe/all/CRN/$id",
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
