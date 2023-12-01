package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.CaseDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.CaseDetail as IntegrationAPICaseDetail

@Component
class ProbationIntegrationEPFGateway(@Value("\${services.probation-integration-epf.base-url}") baseUrl: String) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getCaseDetailForPerson(id: String, eventNumber: Int): Response<IntegrationAPICaseDetail?> {
    return try {
      Response(
        data = webClient.request<CaseDetail?>(
          HttpMethod.GET,
          "/case-details/$id/$eventNumber",
          authenticationHeader(),
        )?.toCaseDetail(),
      )
    } catch (exception: WebClientResponseException.NotFound) {
      Response(
        data = null,
        errors = listOf(
          UpstreamApiError(
            causedBy = UpstreamApi.NDELIUS,
            type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
          ),
        ),
      )
    }
  }

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("nDelius")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
