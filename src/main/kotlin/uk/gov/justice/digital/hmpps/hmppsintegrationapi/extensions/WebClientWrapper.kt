package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import org.springframework.web.reactive.function.client.WebClient

class WebClientWrapper(
  val baseUrl: String,
  val uri: String,
  val authToken: String
) {
  val client: WebClient = WebClient
    .builder()
    .baseUrl(baseUrl)
    .build()

  inline fun <reified T> get(): T = client.get()
    .uri(uri)
    .header("Authorization", "Bearer $authToken")
    .retrieve()
    .bodyToMono(T::class.java)
    .block()
}
