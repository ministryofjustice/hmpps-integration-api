package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

class WebClientWrapper(
  val baseUrl: String,
) {
  @Suppress("ktlint:standard:property-naming")
  val CREATE_TRANSACTION_RETRY_HTTP_CODES = listOf(500, 502, 503, 504, 522, 599, 404, 499, 408, 301)

  val client: WebClient =
    WebClient
      .builder()
      .baseUrl(baseUrl)
      .exchangeStrategies(
        ExchangeStrategies
          .builder()
          .codecs { configurer ->
            configurer
              .defaultCodecs()
              .maxInMemorySize(-1)
          }.build(),
      ).build()

  sealed class WebClientWrapperResponse<out T> {
    data class Success<T>(
      val data: T,
    ) : WebClientWrapperResponse<T>()

    data class Error(
      val errors: List<UpstreamApiError>,
    ) : WebClientWrapperResponse<Nothing>()
  }

  inline fun <reified T> request(
    method: HttpMethod,
    uri: String,
    headers: Map<String, String>,
    upstreamApi: UpstreamApi,
    requestBody: Map<String, Any?>? = null,
    forbiddenAsError: Boolean = false,
  ): WebClientWrapperResponse<T> =
    try {
      val responseData =
        getResponseBodySpec(method, uri, headers, requestBody)
          .retrieve()
          .bodyToMono(T::class.java)
          .block()!!

      WebClientWrapperResponse.Success(responseData)
    } catch (exception: WebClientResponseException) {
      getErrorType(exception, upstreamApi, forbiddenAsError)
    }

  inline fun <reified T> requestWithRetry(
    method: HttpMethod,
    uri: String,
    headers: Map<String, String>,
    upstreamApi: UpstreamApi,
    requestBody: Map<String, Any?>? = null,
    forbiddenAsError: Boolean = false,
  ): WebClientWrapperResponse<T> =
    try {
      val responseData =
        getResponseBodySpec(method, uri, headers, requestBody)
          .retrieve()
          .onStatus({ status -> status.value() in CREATE_TRANSACTION_RETRY_HTTP_CODES }) { response -> Mono.error(ResponseException(null, response.statusCode().value())) }
          .bodyToMono(T::class.java)
          .retryWhen(
            Retry
              .backoff(3, java.time.Duration.ofSeconds(3))
              .filter { throwable -> throwable is ResponseException },
          ).block()!!

      WebClientWrapperResponse.Success(responseData)
    } catch (exception: WebClientResponseException) {
      getErrorType(exception, upstreamApi, forbiddenAsError)
    }

  inline fun <reified T> requestList(
    method: HttpMethod,
    uri: String,
    headers: Map<String, String>,
    upstreamApi: UpstreamApi,
    requestBody: Map<String, Any?>? = null,
    forbiddenAsError: Boolean = false,
  ): WebClientWrapperResponse<List<T>> =
    try {
      val responseData =
        getResponseBodySpec(method, uri, headers, requestBody)
          .retrieve()
          .bodyToFlux(T::class.java)
          .collectList()
          .block() as List<T>

      WebClientWrapperResponse.Success(responseData)
    } catch (exception: WebClientResponseException) {
      getErrorType(exception, upstreamApi, forbiddenAsError)
    }

  fun getResponseBodySpec(
    method: HttpMethod,
    uri: String,
    headers: Map<String, String>,
    requestBody: Map<String, Any?>? = null,
  ): WebClient.RequestBodySpec {
    val responseBodySpec =
      client
        .method(method)
        .uri(uri)
        .headers { header -> headers.forEach { requestHeader -> header.set(requestHeader.key, requestHeader.value) } }

    if (method == HttpMethod.POST && requestBody != null) {
      responseBodySpec.body(BodyInserters.fromValue(requestBody))
    }

    return responseBodySpec
  }

  fun getErrorType(
    exception: WebClientResponseException,
    upstreamApi: UpstreamApi,
    forbiddenAsError: Boolean = false,
  ): WebClientWrapperResponse.Error {
    val errorType =
      when (exception.statusCode) {
        HttpStatus.NOT_FOUND -> UpstreamApiError.Type.ENTITY_NOT_FOUND
        HttpStatus.FORBIDDEN -> if (forbiddenAsError) UpstreamApiError.Type.FORBIDDEN else throw exception
        else -> throw exception
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

inline fun <reified T> WebClientWrapper.WebClientWrapperResponse<T>.getOrError(error: (errors: List<UpstreamApiError>) -> Response<Any?>): T {
  if (this is WebClientWrapper.WebClientWrapperResponse.Error) {
    error(this.errors)
  }
  val success = this as WebClientWrapper.WebClientWrapperResponse.Success
  return success.data
}
