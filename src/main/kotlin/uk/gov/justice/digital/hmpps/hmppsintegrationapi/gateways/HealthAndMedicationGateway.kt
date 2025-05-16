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
) {
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
