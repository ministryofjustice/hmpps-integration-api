package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.GlobalSearch

@Component
class PrisonerOffenderSearchGateway(@Value("\${services.prisoner-offender-search.base-url}") baseUrl: String) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getPersons(firstName: String? = null, lastName: String? = null, hmppsId: String? = null, searchWithinAliases: Boolean = false): Response<List<Person>> {
    val maxNumberOfResults = 9999
    val requestBody =
      mapOf("firstName" to firstName, "lastName" to lastName, "includeAliases" to searchWithinAliases, "prisonerIdentifier" to hmppsId)
        .filterValues { it != null }

    val result = webClient.requestWithErrorHandling<GlobalSearch>(HttpMethod.POST, "/global-search?size=$maxNumberOfResults", authenticationHeader(), UpstreamApi.PRISONER_OFFENDER_SEARCH, requestBody)

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data.content.map { it.toPerson() })
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
    val token = hmppsAuthGateway.getClientToken("Prisoner Offender Search")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
