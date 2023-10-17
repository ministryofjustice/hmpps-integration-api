package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.GlobalSearch
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.Prisoner

@Component
class PrisonerOffenderSearchGateway(@Value("\${services.prisoner-offender-search.base-url}") baseUrl: String) {
  private val webClient = WebClientWrapper(baseUrl)
  private val log = LoggerFactory.getLogger(this::class.java)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getPerson(id: String): Person? {
    return try {
      webClient.request<Prisoner>(HttpMethod.GET, "/prisoner/$id", authenticationHeader()).toPerson()
    } catch (exception: WebClientResponseException.BadRequest) {
      log.error("${exception.message} - ${Json.parseToJsonElement(exception.responseBodyAsString).jsonObject["developerMessage"]}")
      null
    } catch (exception: WebClientResponseException.NotFound) {
      null
    }
  }

  fun getPersons(firstName: String? = null, lastName: String? = null, pncId: String? = null, searchWithinAliases: Boolean = false): Response<List<Person>> {
    val maxNumberOfResults = 9999
    val requestBody =
      mapOf("firstName" to firstName, "lastName" to lastName, "includeAliases" to searchWithinAliases, "prisonerIdentifier" to pncId)
        .filterValues { it != null }

    return Response(
      data = webClient.request<GlobalSearch>(HttpMethod.POST, "/global-search?size=$maxNumberOfResults", authenticationHeader(), requestBody)
        .content.map { it.toPerson() },
    )
  }

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("Prisoner Offender Search")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
