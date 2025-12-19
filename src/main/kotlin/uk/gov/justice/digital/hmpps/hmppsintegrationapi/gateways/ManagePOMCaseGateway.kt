package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonOffenderManager
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.managePOMCase.AllocationPrimaryPOM

@Component
class ManagePOMCaseGateway(
  @Value("\${services.manage-pom-case-api.base-url}") baseUrl: String,
) : UpstreamGateway {
  override fun metaData() =
    GatewayMetadata(
      summary = "Enables the efficient allocation of cases to responsible staff during an individual's time in custody and transition into the community.",
      developerPortalId = "DPS030",
      developerPortalUrl = "https://developer-portal.hmpps.service.justice.gov.uk/components/hmpps-manage-pom-cases-api",
      apiSpecUrl = "https://dev.moic.service.justice.gov.uk/v3/api-docs.json",
      gitHubRepoUrl = "https://github.com/ministryofjustice/hmpps-manage-pom-cases-api",
    )

  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getPrimaryPOMForNomisNumber(nomsNumber: String): Response<PrisonOffenderManager?> {
    val result =
      webClient.request<AllocationPrimaryPOM?>(
        HttpMethod.GET,
        "/api/allocation/$nomsNumber/primary_pom",
        authenticationHeader(),
        UpstreamApi.MANAGE_POM_CASE,
      )

    return when (result) {
      is WebClientWrapper.WebClientWrapperResponse.Success -> {
        Response(data = result.data?.toPrisonOffenderManager())
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
    val token = hmppsAuthGateway.getClientToken("ManagePOMCase")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
