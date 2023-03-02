package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.DataTransferObject.DataTransferObject

class WebClientWrapper(
  val baseUrl: String,
  val uri: String,
  val authToken: String
) {
  val client: WebClient = WebClient
    .builder()
    .baseUrl(baseUrl)
    .build()


  //Use is expected to perform type conversion themselves after getting the response.
  inline fun <reified T> simpleGet(): T? {
    return client.get()
      .uri(uri)
      .header("Authorization", "Bearer $authToken")
      .retrieve()
      .bodyToFlux(T::class.java)
      .blockFirst()
  }

  //Performs type conversion for the user, but the destination object must be of type DataTransferObject<DestType>
  //T: The type to convert from
  //K: The type to convert to
  inline fun <reified T, reified K> complexGet(): K? where T : DataTransferObject<K> {
    return client.get()
      .uri(uri)
      .header("Authorization", "Bearer $authToken")
      .retrieve()
      .bodyToFlux(T::class.java)
      .map { it -> it.toDomain() }
      .blockFirst()
  }
}
