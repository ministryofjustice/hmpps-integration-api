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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.Offender

@Component
class ProbationOffenderSearchGateway(@Value("\${services.probation-offender-search.base-url}") baseUrl: String) {
  private val webClient: WebClient = WebClient.builder().baseUrl(baseUrl).build()
  private val log = LoggerFactory.getLogger(this::class.java)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getPerson(id: String): Person? {
    val token = hmppsAuthGateway.getClientToken("Probation Offender Search")

    return try {
      webClient
        .post()
        .uri("/search")
        .header("Authorization", "Bearer $token")
        .body(BodyInserters.fromValue(mapOf("nomsNumber" to id, "valid" to true)))
        .retrieve()
        .bodyToFlux(Offender::class.java)
        .map { offender -> offender.toPerson() }
        .blockFirst()
    } catch (exception: WebClientResponseException.BadRequest) {
      log.error("${exception.message} - ${Json.parseToJsonElement(exception.responseBodyAsString).jsonObject["developerMessage"]}")
      null
    }
  }

  fun getPersons(firstName: String, surname: String): List<Person> {
    val token = hmppsAuthGateway.getClientToken("Probation Offender Search")

    return webClient
      .post()
      .uri("/search")
      .header("Authorization", "Bearer $token")
      .body(BodyInserters.fromValue(mapOf("firstName" to firstName, "surname" to surname, "valid" to true)))
      .retrieve()
      .bodyToFlux(Offender::class.java)
      .map { offender -> offender.toPerson() }
      .collectList()
      .block() as List<Person>
  }

  fun getAddressesForPerson(id: String): List<Address> {
    val token = hmppsAuthGateway.getClientToken("Probation Offender Search")

    val offender = webClient
      .post()
      .uri("/search")
      .header("Authorization", "Bearer $token")
      .body(BodyInserters.fromValue(mapOf("nomsNumber" to id, "valid" to true)))
      .retrieve()
      .bodyToFlux(Offender::class.java)
      .blockFirst() ?: throw EntityNotFoundException("Could not find person with id: $id")

    return offender.contactDetails.addresses.map { address -> address.toAddress() }
  }
}
