package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner

@Component
class PrisonerSearchGateway(
  @Value("\${services.prisoner-offender-search.base-url}") baseUrl: String,
) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getPrisoner(nomsNumber: String): POSPrisoner? {
    val result =
      webClient.request<POSPrisoner>(
        HttpMethod.GET,
        "/prisoner/$nomsNumber",
        authenticationHeader(),
        UpstreamApi.PRISONER_OFFENDER_SEARCH,
      )

    return when (result) {
      is WebClientWrapper.WebClientWrapperResponse.Success -> {
        result.data
      }

      is WebClientWrapper.WebClientWrapperResponse.Error -> {
        null
      }
    }
  }

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("PRISONER_SEARCH")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
