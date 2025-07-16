package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PlanCreationSchedules
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi

@Component
class SANGateway(
  @Value("\${services.san.base-url}") baseUrl: String,
) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getPlanCreationSchedules(prisonerNumber: String): Response<PlanCreationSchedules> {
    val result =
      webClient.request<PlanCreationSchedules>(
        HttpMethod.GET,
        "/profile/$prisonerNumber/plan-creation-schedule?includeAllHistory=true",
        authenticationHeader(),
        UpstreamApi.SAN,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        val planCreationSchedules = result.data
        Response(data = planCreationSchedules)
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = PlanCreationSchedules(listOf()),
          errors = result.errors,
        )
      }
    }
  }

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("SAN")
    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
