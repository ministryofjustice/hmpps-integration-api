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

  inline fun <reified T> request(method: HttpMethod, uri: String, headers: Map<String, String>, upstreamApi: UpstreamApi, requestBody: Map<String, Any?>? = null): WebClientWrapperResponse<T> {
    return try {
      val responseData = getResponseBodySpec(method, uri, headers, requestBody).retrieve()
        .bodyToMono(T::class.java)
        .block()!!

      WebClientWrapperResponse.Success(responseData)
    } catch (exception: WebClientResponseException) {
      getErrorType(exception, upstreamApi)
    }
  }

  inline fun <reified T> requestList(method: HttpMethod, uri: String, headers: Map<String, String>, upstreamApi: UpstreamApi, requestBody: Map<String, Any?>? = null): WebClientWrapperResponse<List<T>> {
    return try {
      val responseData = getResponseBodySpec(method, uri, headers, requestBody).retrieve()
        .bodyToFlux(T::class.java)
        .collectList()
        .block() as List<T>

      WebClientWrapperResponse.Success(responseData)
    } catch (exception: WebClientResponseException) {
      getErrorType(exception, upstreamApi)
    }
  }
  fun getResponseBodySpec(method: HttpMethod, uri: String, headers: Map<String, String>, requestBody: Map<String, Any?>? = null): WebClient.RequestBodySpec {
    val responseBodySpec = client.method(method)
      .uri(uri)
      .headers { header -> headers.forEach { requestHeader -> header.set(requestHeader.key, requestHeader.value) } }

    if (method == HttpMethod.POST && requestBody != null) {
      responseBodySpec.body(BodyInserters.fromValue(requestBody))
    }

    return responseBodySpec
  }
  fun getErrorType(exception: WebClientResponseException, upstreamApi: UpstreamApi): WebClientWrapperResponse.Error {
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
