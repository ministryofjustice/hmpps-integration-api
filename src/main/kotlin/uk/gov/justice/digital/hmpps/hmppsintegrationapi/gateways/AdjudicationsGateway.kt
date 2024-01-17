package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.adjudications.ReportedAdjudicationDto
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Adjudication
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi

@Component
class AdjudicationsGateway(@Value("\${services.adjudications.base-url}") baseUrl: String) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getReportedAdjudicationsForPerson(id: String): Response<List<Adjudication>> {
    val result = webClient.requestList<ReportedAdjudicationDto>(
      HttpMethod.GET,
      "/reported-adjudications/prisoner/$id",
      authenticationHeader(),
      UpstreamApi.ADJUDICATIONS,

    )

    return when (result) {
      is WebClientWrapper.WebClientWrapperResponse.Success -> {
        Response(data = result.data.map { it.toAdjudication() })
      }

      is WebClientWrapper.WebClientWrapperResponse.Error -> {
        Response(
          data = emptyList(),
          errors = result.errors,
        )
      }
    }
  }

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("Adjudications")

    return mapOf(
      "Authorization" to "Bearer $token",
      "Active-Caseload" to "MDI",
    )
  }
}
