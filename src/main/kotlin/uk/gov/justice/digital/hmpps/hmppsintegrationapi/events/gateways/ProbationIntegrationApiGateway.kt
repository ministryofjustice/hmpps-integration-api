package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.person.PersonExists
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.person.PersonIdentifier

@Component
class ProbationIntegrationApiGateway(
  @Value("\${services.probation-integration.base-url}") baseUrl: String,
) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getPersonIdentifier(nomisId: String): PersonIdentifier? {
    val result =
      webClient.request<PersonIdentifier>(
        HttpMethod.GET,
        "/identifier-converter/noms-to-crn/$nomisId",
        authenticationHeader(),
        UpstreamApi.NDELIUS,
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

  fun getPersonExists(crn: String): PersonExists {
    val result =
      webClient.request<PersonExists>(
        HttpMethod.GET,
        "/exists-in-delius/crn/$crn",
        authenticationHeader(),
        UpstreamApi.NDELIUS,
      )

    return when (result) {
      is WebClientWrapper.WebClientWrapperResponse.Success -> {
        result.data
      }

      is WebClientWrapper.WebClientWrapperResponse.Error -> {
        PersonExists(crn, false)
      }
    }
  }

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("ProbationIntegrationApi")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
