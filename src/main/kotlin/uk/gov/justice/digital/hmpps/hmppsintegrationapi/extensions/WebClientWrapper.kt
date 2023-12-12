package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError

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

  sealed class WebClientWrapperResponse<out T> {
    data class Success<T>(val data: T) : WebClientWrapperResponse<T>()
    data class Error(val errors: List<UpstreamApiError>) : WebClientWrapperResponse<Nothing>()
  }

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

  inline fun <reified T> requestWithErrorHandling(method: HttpMethod, uri: String, headers: Map<String, String>, upstreamApi: UpstreamApi, requestBody: Map<String, Any?>? = null): WebClientWrapperResponse<T> {
    try {
      val responseBodySpec = client.method(method)
        .uri(uri)
        .headers { header -> headers.forEach { requestHeader -> header.set(requestHeader.key, requestHeader.value) } }

      if (method == HttpMethod.POST && requestBody != null) {
        responseBodySpec.body(BodyInserters.fromValue(requestBody))
      }

      val responseData = responseBodySpec.retrieve()
        .bodyToMono(T::class.java)
        .block()!!

      return WebClientWrapperResponse.Success(responseData)
    } catch (exception: WebClientResponseException) {
      val errorType = when (exception.statusCode) {
        HttpStatus.NOT_FOUND -> UpstreamApiError.Type.ENTITY_NOT_FOUND
        HttpStatus.FORBIDDEN -> UpstreamApiError.Type.FORBIDDEN
        HttpStatus.BAD_REQUEST -> UpstreamApiError.Type.BAD_REQUEST
        else -> UpstreamApiError.Type.INTERNAL_SERVER_ERROR
      }
      return WebClientWrapperResponse.Error(
        listOf(
          UpstreamApiError(
            causedBy = upstreamApi,
            type = errorType,
          ),
        ),
      )
    }
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
