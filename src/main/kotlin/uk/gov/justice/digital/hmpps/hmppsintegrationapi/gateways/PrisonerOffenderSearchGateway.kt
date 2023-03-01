package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.GlobalSearch
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.Prisoner

@Component
class PrisonerOffenderSearchGateway(@Value("\${services.prisoner-offender-search.base-url}") baseUrl: String) {
  private val webClient: WebClient = WebClient.builder().baseUrl(baseUrl).build()
  private val log = LoggerFactory.getLogger(this::class.java)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getPerson(id: String): Person? {
    val token = hmppsAuthGateway.getClientToken("Prisoner Offender Search")

    return try {
      webClient
        .get()
        .uri("/prisoner/$id")
        .header("Authorization", "Bearer $token")
        .retrieve()
        .bodyToFlux(Prisoner::class.java)
        .map { prisoner -> prisoner.toPerson() }
        .blockFirst()
    } catch (exception: WebClientResponseException.BadRequest) {
      log.error("${exception.message} - ${Json.parseToJsonElement(exception.responseBodyAsString).jsonObject["developerMessage"]}")
      null
    } catch (exception: WebClientResponseException.NotFound) {
      null
    }
  }

  fun getPersons(firstName: String?, lastName: String?): List<Person> {
    val token = hmppsAuthGateway.getClientToken("Prisoner Offender Search")

    val requestBody = mapOf("firstName" to firstName, "lastName" to lastName, "includeAliases" to true)

    val response = webClient
      .post()
      .uri("/global-search")
      .header("Authorization", "Bearer $token")
      .body(BodyInserters.fromValue(requestBody.filterValues { it != null }))
      .retrieve()
      .bodyToMono(GlobalSearch::class.java)
      .block()

    return response.content.map { it.toPerson() }
  }
}
