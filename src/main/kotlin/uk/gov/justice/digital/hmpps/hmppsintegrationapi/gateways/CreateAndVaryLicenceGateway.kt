package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.createAndVaryLicence.CvlLicence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.createAndVaryLicence.CvlLicenceSummary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Licence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LicenceCondition
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi

@Component
class CreateAndVaryLicenceGateway(@Value("\${services.create-and-vary-licence.base-url}") baseUrl: String) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getLicenceSummaries(id: String): Response<List<Licence>> {
    val result = webClient.requestList<CvlLicenceSummary>(
      HttpMethod.GET,
      "/public/licence-summaries/crn/$id",
      authenticationHeader(),
      UpstreamApi.CVL,
    )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data.map { it.toLicence() })
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = emptyList(),
          errors = result.errors,
        )
      }
    }
  }

  fun getLicenceConditions(id: String): Response<List<LicenceCondition>> {
    val result = webClient.request<CvlLicence>(
      HttpMethod.GET,
      "/public/licences/id/$id",
      authenticationHeader(),
      UpstreamApi.CVL,
    )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data.toLicenceConditions())
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = emptyList(),
          errors = result.errors,
        )
      }
    }
  }

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("CVL")
    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
