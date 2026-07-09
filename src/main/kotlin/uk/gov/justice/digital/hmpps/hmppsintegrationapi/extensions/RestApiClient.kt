package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import com.fasterxml.jackson.annotation.JsonInclude
import io.netty.channel.ChannelOption
import org.springframework.core.codec.DecodingException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.JacksonJsonDecoder
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.util.retry.Retry
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.ResponseException
import java.time.Duration
import java.util.function.Consumer
import kotlin.collections.emptyList
import kotlin.reflect.KClass

/**
 * Mockable client wrapper for making upstream API calls.
 */
class RestApiClient(
  val apiName: String,
  val baseUrl: String,
  val defaultOptions: RestApiOptions = RestApiOptions(),
  val webClient: WebClient? = null,
) {
  val CREATE_TRANSACTION_RETRY_HTTP_CODES = listOf(502, 503, 504, 522, 599, 499, 408)

  fun <T : Any> get(
    path: String,
    responseType: KClass<T>,
    headers: Map<String, String> = mapOf(),
    options: RestApiOptions? = null,
  ): RestApiResponse<T> {
    val opts = options ?: defaultOptions

    val request =
      webClient(opts)
        .method(HttpMethod.GET)
        .uri(path)
        .headers(mapHeaders(headers))

    try {
      var responseSpec = request.retrieve()

      if (opts.retryAttempts > 0) {
        responseSpec =
          responseSpec.onStatus({ status -> status.value() in CREATE_TRANSACTION_RETRY_HTTP_CODES }) { response ->
            retryError(response, apiName, HttpMethod.GET, path)
          }
      }

      var mono = responseSpec.bodyToMono(responseType.java)

      if (opts.retryAttempts > 0) {
        mono = mono.retryWhen(retrySpec(opts))
      }

      val data = mono.block()

      return RestApiResponse(apiName, HttpStatus.OK, data)
    } catch (e: WebClientResponseException) {
      return RestApiResponse(apiName, HttpStatus.valueOf(e.statusCode.value()), null, listOf(e))
    } catch (e: DecodingException) {
      return RestApiResponse(apiName, HttpStatus.OK, null, listOf(e))
    } catch (e: Exception) {
      return RestApiResponse(apiName, null, null, listOf(e))
    }
  }

  private fun mapHeaders(headers: Map<String, String>): Consumer<HttpHeaders> = { header -> headers.forEach { requestHeader -> header.set(requestHeader.key, requestHeader.value) } }

  private fun isSafeToRetry(throwable: Throwable) = throwable is ResponseException || throwable is WebClientRequestException

  private fun retrySpec(opts: RestApiOptions) =
    Retry
      .backoff(opts.retryAttempts, opts.initialBackOffDuration)
      .filter { throwable -> isSafeToRetry(throwable) }
      .onRetryExhaustedThrow { _, retrySignal -> throw ResponseException("External Service failed to process after ${retrySignal.totalRetries()} retries with ${retrySignal.failure().message}", HttpStatus.SERVICE_UNAVAILABLE.value(), retrySignal.failure().cause) }

  private fun httpClient(options: RestApiOptions) =
    HttpClient
      .create()
      .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, options.connectTimeoutMillis)
      .responseTimeout(Duration.ofSeconds(options.responseTimeoutSeconds))

  private val mapper: JsonMapper =
    JsonMapper
      .builder()
      .configureForJackson2()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
      .changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_NULL) }
      .changeDefaultPropertyInclusion { it.withContentInclusion(JsonInclude.Include.NON_NULL) }
      .addModule(KotlinModule.Builder().build())
      .build()

  private val strategies =
    ExchangeStrategies
      .builder()
      .codecs { configurer ->
        configurer
          .defaultCodecs()
          .jacksonJsonDecoder(JacksonJsonDecoder(mapper))
        configurer.defaultCodecs().maxInMemorySize(-1)
      }.build()

  private fun webClient(options: RestApiOptions) =
    webClient ?: WebClient
      .builder()
      .baseUrl(baseUrl)
      .clientConnector(ReactorClientHttpConnector(httpClient(options)))
      .exchangeStrategies(strategies)
      .build()

  fun retryError(
    response: ClientResponse,
    upstreamApi: String,
    method: HttpMethod,
    uri: String,
  ): Mono<ResponseException> =
    Mono.error(
      ResponseException(
        message = "Call to upstream api $upstreamApi failed. ${method.name()} for $uri returned ${response.statusCode().value()}",
        statusCode = response.statusCode().value(),
      ),
    )
}

data class RestApiResponse<T>(
  val apiName: String?,
  val status: HttpStatus?,
  val data: T?,
  var errors: List<Exception> = emptyList(),
)

data class RestApiOptions(
  val connectTimeoutMillis: Int = 10_000,
  val responseTimeoutSeconds: Long = 15,
  val retryAttempts: Long = 3L,
  val initialBackOffDuration: Duration = Duration.ofSeconds(3),
)
