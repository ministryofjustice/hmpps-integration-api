package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person

@Component
class ProbationOffenderSearchGateway(@Value("\${services.probation-offender-search.base-url}") baseUrl: String) {
  private val webClient: WebClient = WebClient.builder().baseUrl(baseUrl).build()

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getPerson(id: String): Person? {
    val token = hmppsAuthGateway.getClientToken("Probation Offender Search")

    return webClient
      .get()
      .uri("/search")
      .contentType(MediaType.APPLICATION_JSON)
      .header("Authorization", "Bearer $token")
      .bodyValue()
      .retrieve()
      .bodyToMono(Person::class.java)
      .block()
  }
}
