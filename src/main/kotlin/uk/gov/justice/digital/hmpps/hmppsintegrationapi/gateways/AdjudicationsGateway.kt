package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.adjudications.Adjudications
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi

@Component
class AdjudicationsGateway(@Value("\${services.adjudications.base-url}") baseUrl: String) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getAdjudicationsForPerson(id: String): Int? {
    val result = webClient.request<Adjudications>(
      HttpMethod.GET,
      "/reported-adjudications/reports",
      authenticationHeader(),
      UpstreamApi.ADJUDICATIONS,

      return 200,
    )
  }

//  fun getAdjudicationsForPerson(id: String): Response<List<Adjudication>> {
//    val result = webClient.request<Adjudications>(
//      HttpMethod.GET,
//      "/reported-adjudications/prisoner/$id",
//      authenticationHeader(),
//      UpstreamApi.ADJUDICATIONS,
//    )
//
//    return when (result) {
//      is WebClientWrapperResponse.Success -> {
//        Response(data = result.data.adjudications.flatMap { it.toAdjudications() })
//      }
//
//      is WebClientWrapperResponse.Error -> {
//        Response(
//          data = emptyList(),
//          errors = result.errors,
//        )
//      }
//    }
//  }

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("adjudications")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
