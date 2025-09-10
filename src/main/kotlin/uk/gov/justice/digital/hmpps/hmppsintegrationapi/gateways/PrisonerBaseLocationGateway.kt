package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerBaseLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi

/**
 * <p><This unreal gateway is pending for refactoring /p>
 *
 * - Actual Gateway shall be created in next enhancement, which pull out logic to an API of another Domain Service.
 * - All business logic shall be implemented inside (to be pulled out later), as the tactical solution
 */
@Component
class PrisonerBaseLocationGateway(
  @Value("\${services.prisoner-base-location.base-url}") baseUrl: String,
) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getPrisonerBaseLocation(nomisNumber: String): Response<PrisonerBaseLocation?> {
    val result =
      webClient.request<PrisonerBaseLocation>(
        HttpMethod.GET,
        "v1/persons/$nomisNumber/prisoner-base-location",
        authenticationHeader(),
        UpstreamApi.PRISONER_BASE_LOCATION,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data)
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
    val token = hmppsAuthGateway.getClientToken("Prisoner Base Location")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
