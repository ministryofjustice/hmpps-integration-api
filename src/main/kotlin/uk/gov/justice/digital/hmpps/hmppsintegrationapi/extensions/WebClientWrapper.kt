package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.Offender

class WebClientWrapper(
  val baseUrl: String,
  val authToken: String
) {
  val client: WebClient = WebClient
    .builder()
    .baseUrl(baseUrl)
    .build()

  // can we make authorisation easier to configure?, it may not always be bearer

  inline fun <reified T> get(uri: String): T? {
    return client.get()
      .uri(uri)
      .header("Authorization", "Bearer $authToken")
      .retrieve()
      .bodyToMono(T::class.java)
      .block()
  }

  inline fun <reified T> post(uri: String, requestBody: Map<String, String>): List<T> {
    return client.post()
      .uri(uri)
      .header("Authorization", "Bearer $authToken")
      .body(BodyInserters.fromValue(requestBody))
      .retrieve()
      .bodyToFlux(T::class.java)
      .collectList()
      .block() as List<T>
  }
}
