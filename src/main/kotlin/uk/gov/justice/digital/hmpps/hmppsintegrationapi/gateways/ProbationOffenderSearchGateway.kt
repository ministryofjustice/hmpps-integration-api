package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person

inline fun <reified T> typeReference() = object : ParameterizedTypeReference<T>() {}

@Component
class ProbationOffenderSearchGateway(@Value("\${services.probation-offender-search.base-url}") baseUrl: String) {
  private val webClient: WebClient = WebClient.builder().baseUrl(baseUrl).build()

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getPerson(id: String): Person? {
    val token = hmppsAuthGateway.getClientToken("Probation Offender Search")

    val bodyValues: Map<String, String> = mapOf("nomsNumber" to id)

    val results = webClient
      .post()
      .uri("/search")
      .header("Authorization", "Bearer $token")
      .body(BodyInserters.fromValue(bodyValues))
      .retrieve()
      .bodyToMono(typeReference<List<Person>>())
      .block()

    return results.first()
  }
}
