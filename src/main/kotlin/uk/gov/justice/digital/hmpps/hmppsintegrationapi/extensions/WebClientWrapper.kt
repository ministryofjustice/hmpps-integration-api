package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import io.netty.channel.ChannelOption
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.util.retry.Retry
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.ResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.time.Duration

@Suppress("ktlint:standard:property-naming")
class WebClientWrapper(
  val baseUrl: String,
  connectTimeoutMillis: Int = 10_000,
  responseTimeoutSeconds: Long = 15,
) {
  val CREATE_TRANSACTION_RETRY_HTTP_CODES = listOf(502, 503, 504, 522, 599, 499, 408)
  val MAX_RETRY_ATTEMPTS = 3L
  val MIN_BACKOFF_DURATION = Duration.ofSeconds(3)

  val httpClient =
    HttpClient
      .create()
      .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMillis)
      .responseTimeout(Duration.ofSeconds(responseTimeoutSeconds))

  val client: WebClient =
    WebClient
      .builder()
      .baseUrl(baseUrl)
      .clientConnector(ReactorClientHttpConnector(httpClient))
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
    requestBody: Any? = null,
    forbiddenAsError: Boolean = false,
    badRequestAsError: Boolean = false,
  ): WebClientWrapperResponse<T> =
    if (method == HttpMethod.GET) {
      requestWithRetry(method, uri, headers, upstreamApi, requestBody, forbiddenAsError, badRequestAsError)
    } else {
      try {
        val responseData =
          getResponseBodySpec(method, uri, headers, requestBody)
            .retrieve()
            .bodyToMono(T::class.java)
            .block()!!

        WebClientWrapperResponse.Success(responseData)
      } catch (exception: WebClientResponseException) {
        getErrorType(exception, upstreamApi, forbiddenAsError, badRequestAsError)
      }
    }

  /**
   * Warning: This function should only be used with IDEMPOTENT requests.
   * If your POST request is not idempotent then you should not use this function.
   */
  inline fun <reified T> requestWithRetry(
    method: HttpMethod,
    uri: String,
    headers: Map<String, String>,
    upstreamApi: UpstreamApi,
    requestBody: Any? = null,
    forbiddenAsError: Boolean = false,
    badRequestAsError: Boolean = false,
  ): WebClientWrapperResponse<T> =
    try {
      val responseData =
        getResponseBodySpec(method, uri, headers, requestBody)
          .retrieve()
          .onStatus({ status -> status.value() in CREATE_TRANSACTION_RETRY_HTTP_CODES }) { response ->
            retryError(response, upstreamApi, method, uri)
          }.bodyToMono(T::class.java)
          .retryWhen(
            Retry
              .backoff(MAX_RETRY_ATTEMPTS, MIN_BACKOFF_DURATION)
              .filter { throwable -> isSafeToRetry(throwable) }
              .onRetryExhaustedThrow { _, retrySignal -> throw ResponseException("External Service failed to process after ${retrySignal.totalRetries()} retries with ${retrySignal.failure().message}", HttpStatus.SERVICE_UNAVAILABLE.value(), retrySignal.failure().cause) },
          ).block()!!

      WebClientWrapperResponse.Success(responseData)
    } catch (exception: WebClientResponseException) {
      getErrorType(exception, upstreamApi, forbiddenAsError, badRequestAsError)
    }

  fun isSafeToRetry(throwable: Throwable) = throwable is ResponseException || throwable is WebClientRequestException

  inline fun <reified T> requestList(
    method: HttpMethod,
    uri: String,
    headers: Map<String, String>,
    upstreamApi: UpstreamApi,
    requestBody: Any? = null,
    forbiddenAsError: Boolean = false,
    badRequestAsError: Boolean = false,
  ): WebClientWrapperResponse<List<T>> =
    if (method == HttpMethod.GET) {
      requestListWithRetry(method, uri, headers, upstreamApi, requestBody, forbiddenAsError, badRequestAsError)
    } else {
      try {
        val responseData =
          getResponseBodySpec(method, uri, headers, requestBody)
            .retrieve()
            .bodyToFlux(T::class.java)
            .collectList()
            .block() as List<T>

        WebClientWrapperResponse.Success(responseData)
      } catch (exception: WebClientResponseException) {
        getErrorType(exception, upstreamApi, forbiddenAsError, badRequestAsError)
      }
    }

  inline fun <reified T> requestListWithRetry(
    method: HttpMethod,
    uri: String,
    headers: Map<String, String>,
    upstreamApi: UpstreamApi,
    requestBody: Any? = null,
    forbiddenAsError: Boolean = false,
    badRequestAsError: Boolean = false,
  ): WebClientWrapperResponse<List<T>> =
    try {
      val responseData =
        getResponseBodySpec(method, uri, headers, requestBody)
          .retrieve()
          .onStatus({ status -> status.value() in CREATE_TRANSACTION_RETRY_HTTP_CODES }) { response ->
            retryError(response, upstreamApi, method, uri)
          }.bodyToFlux(T::class.java)
          .retryWhen(
            Retry
              .backoff(MAX_RETRY_ATTEMPTS, MIN_BACKOFF_DURATION)
              .filter { throwable -> isSafeToRetry(throwable) }
              .onRetryExhaustedThrow { _, retrySignal -> throw ResponseException("External Service failed to process after ${retrySignal.totalRetries()} retries with ${retrySignal.failure().message}", HttpStatus.SERVICE_UNAVAILABLE.value(), retrySignal.failure().cause) },
          ).collectList()
          .block() as List<T>

      WebClientWrapperResponse.Success(responseData)
    } catch (exception: WebClientResponseException) {
      getErrorType(exception, upstreamApi, forbiddenAsError, badRequestAsError)
    }

  fun retryError(
    response: ClientResponse,
    upstreamApi: UpstreamApi,
    method: HttpMethod,
    uri: String,
  ): Mono<ResponseException> =
    Mono.error(
      ResponseException(
        message = "Call to upstream api ${upstreamApi.name} failed. ${method.name()} for $uri returned ${response.statusCode().value()}",
        statusCode = response.statusCode().value(),
      ),
    )

  fun getResponseBodySpec(
    method: HttpMethod,
    uri: String,
    headers: Map<String, String>,
    requestBody: Any? = null,
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
    badRequestAsError: Boolean = false,
  ): WebClientWrapperResponse.Error {
    val errorType =
      when (exception.statusCode) {
        HttpStatus.NOT_FOUND -> UpstreamApiError.Type.ENTITY_NOT_FOUND
        HttpStatus.CONFLICT -> UpstreamApiError.Type.CONFLICT
        HttpStatus.FORBIDDEN -> if (forbiddenAsError) UpstreamApiError.Type.FORBIDDEN else throw exception
        HttpStatus.BAD_REQUEST -> if (badRequestAsError) UpstreamApiError.Type.BAD_REQUEST else throw exception
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
