package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.healthandmedication.HAMHealthAndMedication
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi

@Component
class HealthAndMedicationGateway(
  @Value("\${services.health-and-medication.base-url}") baseUrl: String,
) : UpstreamGateway {
  override fun metaData() =
    GatewayMetadata(
      summary = "An API providing access to HMPPS health and medication data.",
      developerPortalId = "DPS013",
      developerPortalUrl = "https://developer-portal.hmpps.service.justice.gov.uk/components/hmpps-health-and-medication-api",
      apiDocUrl = "https://health-and-medication-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html",
      apiSpecUrl = "https://health-and-medication-api-dev.hmpps.service.justice.gov.uk/v3/api-docs",
      gitHubRepoUrl = "https://github.com/ministryofjustice/hmpps-health-and-medication-api",
    )

  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("HEALTH_AND_MEDICATION")
    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }

  fun getHealthAndMedicationData(prisonerNumber: String): Response<HAMHealthAndMedication?> {
    val result =
      webClient.request<HAMHealthAndMedication>(
        HttpMethod.GET,
        "/prisoners/$prisonerNumber",
        authenticationHeader(),
        UpstreamApi.HEALTH_AND_MEDICATION,
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
