package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person

@Component
class NomisGateway {
  fun getPerson(id: String): Person? {
    val getOffenderUrl = "http://localhost:8081/api/offenders/$id"
    val builder: WebClient.Builder = WebClient.builder()

    return builder.build()
      .get()
      .uri(getOffenderUrl)
      .retrieve()
      .bodyToMono(Person::class.java)
      .block()
  }
}
