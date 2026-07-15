package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.core.codec.DecodingException
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Pure Kotlin unit tests for the RestApiClient.
 *
 * Because the underlying WebClient and HttpClient classes are so hard to mock
 * these tests mainly focus on verifying the internal helper methods. Testing of
 * the primary functionality, other than some very basic paths, is left for
 * integration tests.
 */
class RestApiClientTest :
  DescribeSpec({
    describe("basic RestApiClient functionality") {
      val defaultClient = RestApiClient("TestAPI", "http://localhost")
      val nonRetryOptions = RestApiOptions(retryAttempts = 0)
      val error500 = WebClientResponseException(500, "Server error 543", null, null, null)
      val decodingError = DecodingException("Decoding error 987")
      val genericError = RuntimeException("Generic error 555")

      fun buildMockClient(
        responseBody: String?,
        httpError: Exception? = null,
        retrySuccess: Boolean = false,
      ): WebClient {
        val webClient = mock(WebClient::class.java, RETURNS_DEEP_STUBS)
        val requestSpec: WebClient.RequestBodySpec = mock()
        val responseSpec: WebClient.ResponseSpec = mock()
        val mono: Mono<String> = mock()
        val flux: Flux<String> = mock()

        whenever(webClient.method(any()).uri(anyString()).headers(any())).thenReturn(requestSpec)

        if (httpError == null) {
          whenever(requestSpec.retrieve()).thenReturn(responseSpec)
        } else {
          if (retrySuccess) {
            whenever(requestSpec.retrieve()).thenThrow(httpError).thenReturn(responseSpec)
          } else {
            whenever(requestSpec.retrieve()).thenThrow(httpError)
          }
        }

        whenever(responseSpec.bodyToMono(String::class.java)).thenReturn(mono)
        whenever(mono.block()).thenReturn(responseBody)

        whenever(responseSpec.bodyToFlux(String::class.java)).thenReturn(flux)
        val listMono: Mono<List<String>> = mock()
        whenever(flux.collectList()).thenReturn(listMono)
        whenever(listMono.block()).thenReturn(listOf(responseBody!!))

        return webClient
      }

      it("should handle a successful GET request") {
        val webClient = buildMockClient("It works!")

        val client = RestApiClient("TestAPI", "http://localhost:8765", nonRetryOptions, webClient)

        val response = client.get("/test", String::class)

        response.status shouldBe HttpStatus.OK
        response.errors.size shouldBe 0
        response.data shouldBe "It works!"
      }

      it("should handle a successful GET request with list responses") {
        val webClient = buildMockClient("Lists work!")
        val client = RestApiClient("TestAPI", "http://localhost:8765", nonRetryOptions, webClient)

        val response = client.getList("/test", String::class)

        response.status shouldBe HttpStatus.OK
        response.errors.size shouldBe 0
        response.data shouldBe listOf("Lists work!")
      }

      it("should handle a POST request") {
        val webClient = buildMockClient("POST works")
        val client = RestApiClient("TestAPI", "http://localhost:8765", nonRetryOptions, webClient)

        val response = client.post("/test", "Query details", String::class)

        response.status shouldBe HttpStatus.OK
        response.errors.size shouldBe 0
        response.data shouldBe "POST works"
      }

      it("should handle a POST with a list response") {
        val webClient = buildMockClient("Lists work!")
        val client = RestApiClient("TestAPI", "http://localhost:8765", nonRetryOptions, webClient)

        val response = client.postForList("/test", "Query details", String::class)

        response.status shouldBe HttpStatus.OK
        response.errors.size shouldBe 0
        response.data shouldBe listOf("Lists work!")
      }

      it("should handle HTTP errors") {
        val webClient = buildMockClient("It fails", error500)
        val client = RestApiClient("TestAPI", "http://localhost:8765", nonRetryOptions, webClient)

        val response = client.get("/test", String::class)

        response.status shouldBe HttpStatus.INTERNAL_SERVER_ERROR
        response.errors.size shouldBe 1
        response.errors[0].message shouldBe "500 Server error 543"
        response.data shouldBe null
      }

      it("should handle decoding errors") {
        val webClient = buildMockClient("It fails", decodingError)
        val client = RestApiClient("TestAPI", "http://localhost:8765", nonRetryOptions, webClient)

        val response = client.get("/test", String::class)

        response.errors.size shouldBe 1
        response.errors[0].message shouldBe "Decoding error 987"
        response.data shouldBe null
      }

      it("should handle generic errors") {
        val webClient = buildMockClient("It fails", genericError)
        val client = RestApiClient("TestAPI", "http://localhost:8765", nonRetryOptions, webClient)

        val response = client.get("/test", String::class)

        response.errors.size shouldBe 1
        response.errors[0].message shouldBe "Generic error 555"
      }

      it("can build a WebClient") {
        val options = RestApiOptions(retryAttempts = 0)

        val webClient = defaultClient.webClient(options)

        webClient shouldNotBe null
      }

      it("can build a RetrySpec") {
        val options = RestApiOptions(retryAttempts = 77)
        defaultClient.retrySpec(options) shouldNotBe null
      }

      it("can build a RetryError") {
        val response = mock(ClientResponse::class.java)
        whenever(response.statusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR)

        val error = defaultClient.retryError(response, "SomeAPI", HttpMethod.GET, "http://localhost:8765")

        error shouldNotBe null
      }

      it("should not retry a 500 error") {
        defaultClient.isSafeToRetry(error500) shouldBe false
      }

      it("can create a retryable request") {
        val requestSpec: WebClient.RequestBodySpec = mock()
        val mockResponse: WebClient.ResponseSpec = mock()
        whenever(requestSpec.retrieve()).thenReturn(mockResponse)
        whenever(mockResponse.onStatus(any(), any())).thenReturn(mockResponse)
        val options = RestApiOptions(retryAttempts = 1)

        val responseSpec = defaultClient.retrieveWithOptionalRetry(requestSpec, "/", options)

        responseSpec shouldNotBe null
      }

      it("can set up auth headers") {
        val headers = defaultClient.authHeaders("JKDSHF8GASKJDSFH")
        headers shouldNotBe null
        headers.size shouldBe 1
        headers["Authorization"] shouldBe "Bearer JKDSHF8GASKJDSFH"
      }
    }
  })
