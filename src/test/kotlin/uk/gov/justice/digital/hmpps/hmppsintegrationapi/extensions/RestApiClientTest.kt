package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

class RestApiClientTest :
  DescribeSpec({
    describe("basic RestApiClient functionality") {
      fun buildMockClient(responseBody: String?, httpError: Exception? = null, retrySuccess: Boolean = false): WebClient {
        val webClient = mock(WebClient::class.java, RETURNS_DEEP_STUBS)
        val requestSpec = mock(WebClient.RequestBodySpec::class.java)
        val responseSpec = mock(WebClient.ResponseSpec::class.java)
        val mono: Mono<String> = mock(Mono::class.java) as Mono<String>
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
        return webClient
      }

      it("should handle a successful GET request") {
        val webClient = buildMockClient("It works!")

        val options = RestApiOptions(retryAttempts = 0)
        val client = RestApiClient("TestAPI", "http://localhost:8765", webClient = webClient, defaultOptions = options)

        val response = client.get("/test", String::class)

        response.status shouldBe HttpStatus.OK
        response.errors.size shouldBe 0
        response.data shouldBe "It works!"
      }

      it("should handle HTTP errors") {
        val webClient = buildMockClient("It fails", WebClientResponseException(500,"Server error 543", null, null, null))

        val options = RestApiOptions(retryAttempts = 0)
        val client = RestApiClient("TestAPI", "http://localhost:8765", webClient = webClient, defaultOptions = options)

        val response = client.get("/test", String::class)

        response.status shouldBe HttpStatus.INTERNAL_SERVER_ERROR
        response.errors.size shouldBe 1
        response.errors[0].message shouldBe "500 Server error 543"
        response.data shouldBe null
      }
    }
  })
