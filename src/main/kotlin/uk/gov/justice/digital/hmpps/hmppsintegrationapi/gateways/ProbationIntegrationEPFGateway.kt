package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.EPF_ENDPOINT_INCLUDES_LAO
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationintegrationepf.EPFCaseDetail

@Component
class ProbationIntegrationEPFGateway(
  @Value("\${services.probation-integration-epf.base-url}") baseUrl: String,
  @Autowired val featureFlag: FeatureFlagConfig,
) : UpstreamGateway {
  override fun metaData() =
    GatewayMetadata(
      summary = "DEPRECATED - Delius integration API specifically for the Effective Proposals Framework",
      apiDocUrl = "https://effective-proposal-framework-and-delius-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html#/",
      gitHubRepoUrl = "https://github.com/ministryofjustice/hmpps-probation-integration-services/tree/main/projects/effective-proposal-framework-and-delius",
    )

  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  @Deprecated("Use NDeliusGateway.getEpfCaseDetailForPerson instead")
  fun getCaseDetailForPerson(
    id: String,
    eventNumber: Int,
  ): Response<CaseDetail?> {
    val result =
      webClient.request<EPFCaseDetail?>(
        HttpMethod.GET,
        "/case-details/$id/$eventNumber",
        authenticationHeader(),
        UpstreamApi.EFFECTIVE_PROPOSAL_FRAMEWORK,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data?.toCaseDetail(includeLimitedAccess = featureFlag.isEnabled(EPF_ENDPOINT_INCLUDES_LAO)))
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
    val token = hmppsAuthGateway.getClientToken("nDelius")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
