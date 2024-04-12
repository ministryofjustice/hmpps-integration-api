package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonOfficerManager
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.managePOMCase.AllocationPrimaryPOM

@Component
class ManagePOMCaseGateway(
  @Value("\${services.manage-pom-case-api.base-url}") baseUrl: String,
) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getPrimaryPOMForNomisNumber(id: String): Response<PrisonOfficerManager> {
    val result =
      webClient.request<AllocationPrimaryPOM>(
        HttpMethod.GET,
        "/allocation/$id/primary_pom",
        authenticationHeader(),
        UpstreamApi.MANAGE_POM_CASE,
      )

    return when (result) {
      is WebClientWrapper.WebClientWrapperResponse.Success -> {
        Response(data = result.data.toPrisonOfficerManager())
      }

      is WebClientWrapper.WebClientWrapperResponse.Error -> {
        Response(
          data = PrisonOfficerManager(),
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
