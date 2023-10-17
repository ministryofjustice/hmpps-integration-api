package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

class WebClientWrapper(
  val baseUrl: String,
) {
  val client: WebClient = WebClient
    .builder()
    .baseUrl(baseUrl)
    .exchangeStrategies(
      ExchangeStrategies.builder()
        .codecs { configurer ->
          configurer.defaultCodecs()
            .maxInMemorySize(-1)
        }
        .build(),
    )
    .build()

  inline fun <reified T> request(method: HttpMethod, uri: String, headers: Map<String, String>, requestBody: Map<String, Any?>? = null): T {
    val responseBodySpec = client.method(method)
      .uri(uri)
      .headers { header -> headers.forEach { requestHeader -> header.set(requestHeader.key, requestHeader.value) } }

    if (method == HttpMethod.POST && requestBody != null) {
      responseBodySpec.body(BodyInserters.fromValue(requestBody))
    }

    return responseBodySpec.retrieve()
      .bodyToMono(T::class.java)
      .block()!!
  }

  inline fun <reified T> requestList(method: HttpMethod, uri: String, headers: Map<String, String>, requestBody: Map<String, Any?>? = null): List<T> {
    val responseBodySpec = client.method(method)
      .uri(uri)
      .headers { header -> headers.forEach { requestHeader -> header.set(requestHeader.key, requestHeader.value) } }

    if (method == HttpMethod.POST && requestBody != null) {
      responseBodySpec.body(BodyInserters.fromValue(requestBody))
    }

    return responseBodySpec.retrieve()
      .bodyToFlux(T::class.java)
      .collectList()
      .block() as List<T>
  }
}
