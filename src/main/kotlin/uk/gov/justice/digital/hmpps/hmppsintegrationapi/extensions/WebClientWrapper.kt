package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.DataTransferObject.DataTransferObject

class WebClientWrapper(
  val baseUrl: String,
  val authToken: String
) {
  val client: WebClient = WebClient
    .builder()
    .baseUrl(baseUrl)
    .build()

  // notes: Make uri part of the method
  // can we make authorisation easier to configure?, it may not always be bearer
  // what if we want one or many objects? How do we do this?

  /**
   * Perform a GET request on an endpoint.
   * @param <T> The type expected back from the request
   * @param <K> The type to convert to when returning the object
   * @return A domain object
   */
  inline fun <reified T, K> getMany(uri: String): List<K> where T : DataTransferObject<K> {
    return client.get()
      .uri(uri)
      .header("Authorization", "Bearer $authToken")
      .retrieve()
      .bodyToFlux(T::class.java)
      .map { it.toDomain() }
      .collectList()
      .block() as List<K>
  }

  inline fun <reified T, K> getOne(uri: String): K? where T : DataTransferObject<K> {
    return client.get()
      .uri(uri)
      .header("Authorization", "Bearer $authToken")
      .retrieve()
      .bodyToMono(T::class.java)
      .map { it.toDomain() }
      .block()
  }
}
