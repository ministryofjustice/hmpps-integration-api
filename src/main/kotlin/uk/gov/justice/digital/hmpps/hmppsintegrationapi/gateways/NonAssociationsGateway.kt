package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nonAssociation.NonAssociationsResponse

@Component
class NonAssociationsGateway(
  @Value("\${services.non-associations.base-url}") baseUrl: String,
) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getNonAssociationsForPerson(
    prisonerNumber: String,
    includeOpen: String? = "true",
    includeClosed: String? = "false",
  ): Response<NonAssociationsResponse?> {
    val result =
      webClient.request<NonAssociationsResponse>(
        HttpMethod.GET,
        "/prisoner/$prisonerNumber/non-associations?includeOpen=$includeOpen&includeClosed=$includeClosed",
        authenticationHeader(),
        UpstreamApi.NON_ASSOCIATIONS,
      )
    return when (result) {
      is WebClientWrapper.WebClientWrapperResponse.Success -> {
        Response(
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

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("NON-ASSOCIATIONS")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
