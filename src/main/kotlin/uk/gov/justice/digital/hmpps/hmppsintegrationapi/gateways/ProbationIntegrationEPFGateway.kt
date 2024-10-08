package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationintegrationepf.EPFCaseDetail

@Component
class ProbationIntegrationEPFGateway(
  @Value("\${services.probation-integration-epf.base-url}") baseUrl: String,
) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getCaseDetailForPerson(
    id: String,
    eventNumber: Int,
  ): Response<CaseDetail?> {
    val result =
      webClient.request<EPFCaseDetail?>(
        HttpMethod.GET,
        "/case-details/$id/$eventNumber",
        authenticationHeader(),
        UpstreamApi.NDELIUS,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data?.toCaseDetail())
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
