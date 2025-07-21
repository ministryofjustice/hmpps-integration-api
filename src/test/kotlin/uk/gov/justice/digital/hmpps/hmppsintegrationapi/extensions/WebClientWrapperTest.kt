package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.exactly
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.kotest.assertions.fail
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.inspectors.shouldForAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.TestApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File
import java.time.Duration

data class StringModel(
  val result: String,
)

data class TestModel(
  val sourceName: String,
  val sourceLastName: String?,
) {
  fun toDomain() = TestDomainModel(sourceName, sourceLastName)
}

data class SearchModel(
  val content: List<TestModel>,
)

data class TestDomainModel(
  val firstName: String,
  val lastName: String?,
)

class WebClientWrapperTest :
  DescribeSpec({
    val mockServer = TestApiMockServer()
    lateinit var webClient: WebClientWrapper
    lateinit var wrapper: WebClientWrapper

    val id = "ABC1234"
    val getPath = "/test/$id"
    val postPath = "/testPost"
    val headers = mapOf("foo" to "bar")
    val featureFlagConfig = mock(FeatureFlagConfig::class.java)

    beforeEach {
      mockServer.start()
      mockServer.resetAll()
      webClient =
        WebClientWrapper(
          baseUrl = mockServer.baseUrl(),
          connectTimeoutMillis = 500,
          responseTimeoutSeconds = 1,
          featureFlagConfig = featureFlagConfig,
        )
      wrapper = spy(webClient)
      whenever(wrapper.MIN_BACKOFF_DURATION).thenReturn(Duration.ofSeconds(1L))
      whenever(wrapper.MAX_RETRY_ATTEMPTS).thenReturn(1L)
    }

    afterTest {
      mockServer.stop()
    }

    describe("when retry on all upstream gets is enabled") {
      beforeEach {
        whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.RETRY_ALL_UPSTREAM_GETS)).thenReturn(true)
      }

      it("calls requestWithRetry for a GET request") {
        listOf(502, 503, 504, 522, 599, 499, 408).forEach {
          mockServer.resetAll()
          mockServer.stubForRetry(it.toString(), getPath, 2, it, 200, """{"sourceName" : "Harold"}""".removeWhitespaceAndNewlines())
          val result = wrapper.request<TestModel>(HttpMethod.GET, getPath, headers, UpstreamApi.TEST)
          result.shouldBeInstanceOf<WebClientWrapperResponse.Success<TestModel>>()
          val testDomainModel = result.data.toDomain()
          mockServer.verify(exactly(2), getRequestedFor(urlEqualTo(getPath)))
          testDomainModel.firstName.shouldBe("Harold")
        }
      }

      it("does not call requestWithRetry for a POST request") {
        mockServer.stubPostTest(
          postPath,
          """
            {
              "content":
              [
                {
                  "sourceName": "Paul",
                  "sourceLastName": "Paper"
                },
                {
                  "sourceName": "Paul",
                  "sourceLastName": "Card"
                }
              ]
            }
            """.removeWhitespaceAndNewlines(),
          HttpStatus.BAD_GATEWAY,
        )

        assertThrows<WebClientResponseException> {
          webClient.request<SearchModel>(HttpMethod.POST, postPath, headers, UpstreamApi.TEST, mapOf("sourceName" to "Paul"))
        }
        mockServer.verify(exactly(1), postRequestedFor(urlEqualTo(postPath)))
      }

      it("calls requestWithRetry for a GET requestList") {
        mockServer.stubForRetry("2", getPath, 2, 502, 200, """[{"sourceName" : "Harold"}]""".removeWhitespaceAndNewlines())
        val result = wrapper.requestList<TestModel>(HttpMethod.GET, getPath, headers, UpstreamApi.TEST)
        result.shouldBeInstanceOf<WebClientWrapperResponse.Success<List<TestModel>>>()
        val testDomainModel = result.data[0].toDomain()
        mockServer.verify(exactly(2), getRequestedFor(urlEqualTo(getPath)))
        testDomainModel.firstName.shouldBe("Harold")
      }

      it("does not call requestWithRetry for a POST requestList") {
        mockServer.stubPostTest(
          postPath,
          """
            {
              "content":
              [
                {
                  "sourceName": "Paul",
                  "sourceLastName": "Paper"
                },
                {
                  "sourceName": "Paul",
                  "sourceLastName": "Card"
                }
              ]
            }
            """.removeWhitespaceAndNewlines(),
          HttpStatus.BAD_GATEWAY,
        )

        assertThrows<WebClientResponseException> {
          webClient.requestList<SearchModel>(HttpMethod.POST, postPath, headers, UpstreamApi.TEST, mapOf("sourceName" to "Paul"))
        }
        mockServer.verify(exactly(1), postRequestedFor(urlEqualTo(postPath)))
      }
    }

    describe("when retry on all upstream gets is disabled") {
      beforeEach {
        whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.RETRY_ALL_UPSTREAM_GETS)).thenReturn(false)
      }

      it("does not call requestWithRetry for a GET request and continues to fail on first attempt") {
        mockServer.stubForRetry("3", getPath, 2, 502, 200, """[{"sourceName" : "Harold"}]""".removeWhitespaceAndNewlines())
        assertThrows<WebClientResponseException> { wrapper.requestList<TestModel>(HttpMethod.GET, getPath, headers, UpstreamApi.TEST) }
        mockServer.verify(exactly(1), getRequestedFor(urlEqualTo(getPath)))
      }

      it("does not call requestWithRetry for a GET requestList and continues to fail on first attempt") {
        mockServer.stubForRetry("4", getPath, 2, 502, 200, """[{"sourceName" : "Harold"}]""".removeWhitespaceAndNewlines())
        assertThrows<WebClientResponseException> { wrapper.requestList<TestModel>(HttpMethod.GET, getPath, headers, UpstreamApi.TEST) }
        mockServer.verify(exactly(1), getRequestedFor(urlEqualTo(getPath)))
      }
    }

    describe("when webClientWrapperResponse is Success") {
      describe("when request") {
        it("performs a GET request where the result is a json object") {
          mockServer.stubGetTest(getPath, """{"sourceName" : "Harold"}""".removeWhitespaceAndNewlines())

          val result = webClient.request<TestModel>(HttpMethod.GET, getPath, headers, UpstreamApi.TEST)
          result.shouldBeInstanceOf<WebClientWrapperResponse.Success<TestModel>>()
          val testDomainModel = result.data.toDomain()
          testDomainModel.firstName.shouldBe("Harold")
        }

        it("performs a POST request where the response is a json object") {
          mockServer.stubPostTest(
            postPath,
            """
            {
              "content":
              [
                {
                  "sourceName": "Paul",
                  "sourceLastName": "Paper"
                },
                {
                  "sourceName": "Paul",
                  "sourceLastName": "Card"
                }
              ]
            }
            """.removeWhitespaceAndNewlines(),
          )

          val result = webClient.request<SearchModel>(HttpMethod.POST, postPath, headers, UpstreamApi.TEST, mapOf("sourceName" to "Paul"))
          result.shouldBeInstanceOf<WebClientWrapperResponse.Success<SearchModel>>()
          val testDomainModels = result.data.content.map { it.toDomain() }
          testDomainModels.shouldForAll { it.firstName.shouldBe("Paul") }
          testDomainModels.first().lastName.shouldBe("Paper")
          testDomainModels.last().lastName.shouldBe("Card")
        }

        it("performs a POST request where the request body is an array") {
          mockServer.stubPostTest(postPath, """{"result": "success"}""")

          val result =
            webClient.request<StringModel>(
              HttpMethod.POST,
              postPath,
              headers,
              UpstreamApi.TEST,
              listOf("Paul"),
            )
          mockServer.verify(
            postRequestedFor(urlEqualTo(postPath))
              .withRequestBody(equalToJson("[\"Paul\"]"))
              .withHeader("Content-Type", equalTo("application/json")),
          )
          result.shouldBeInstanceOf<WebClientWrapperResponse.Success<StringModel>>()
          result.data.result.shouldBe("success")
        }

        it("performs a GET request with multiple headers") {
          mockServer.stubGetTest(getPath, """{"result": "headers matched"}""")

          val headers =
            mapOf(
              "foo" to "bar",
              "bar" to "baz",
            )

          val result = webClient.request<StringModel>(HttpMethod.GET, getPath, headers = headers, UpstreamApi.TEST)
          mockServer.verify(
            getRequestedFor(urlEqualTo(getPath))
              .withHeader("foo", equalTo(headers["foo"]))
              .withHeader("bar", equalTo(headers["bar"])),
          )
          result.shouldBeInstanceOf<WebClientWrapperResponse.Success<StringModel>>()
          result.data.result.shouldBe("headers matched")
        }
      }

      describe("when requestList") {
        it("performs a GET request where the response is an array") {
          mockServer.stubGetTest(
            getPath,
            """
            [
              {
                "sourceName": "Paul"
              },
              {
                "sourceName": "Paul"
              }
            ]
            """.removeWhitespaceAndNewlines(),
          )

          val result =
            webClient.requestList<TestModel>(
              HttpMethod.GET,
              getPath,
              headers,
              UpstreamApi.TEST,
            )
          result.shouldBeInstanceOf<WebClientWrapperResponse.Success<List<TestModel>>>()
          val testDomainModels = result.data.map { it.toDomain() }
          testDomainModels.shouldForAll { it.firstName.shouldBe("Paul") }
        }

        it("performs a POST request where the response is an array") {
          mockServer.stubPostTest(
            postPath,
            """
            [
              {
                "sourceName": "Paul"
              },
              {
                "sourceName": "Paul"
              }
            ]
            """.removeWhitespaceAndNewlines(),
          )

          val result =
            webClient.requestList<TestModel>(
              HttpMethod.POST,
              postPath,
              headers,
              UpstreamApi.TEST,
              mapOf("sourceName" to "Paul"),
            )
          result.shouldBeInstanceOf<WebClientWrapperResponse.Success<List<TestModel>>>()
          val testDomainModels = result.data.map { it.toDomain() }
          testDomainModels.shouldForAll { it.firstName.shouldBe("Paul") }
        }

        it("performs a POST request where the request body is an array") {
          mockServer.stubPostTest(postPath, """{"result": "success"}""")

          val result =
            webClient.requestList<StringModel>(
              HttpMethod.POST,
              postPath,
              headers,
              UpstreamApi.TEST,
              listOf("Paul"),
            )

          mockServer.verify(
            postRequestedFor(urlEqualTo(postPath))
              .withRequestBody(equalToJson("[\"Paul\"]"))
              .withHeader("Content-Type", equalTo("application/json")),
          )
          result.shouldBeInstanceOf<WebClientWrapperResponse.Success<List<StringModel>>>()
          result.data
            .first()
            .result
            .shouldBe("success")
        }

        it("performs a GET request with multiple headers") {
          mockServer.stubGetTest(getPath, """{"result": "headers matched"}""")

          val headers =
            mapOf(
              "foo" to "bar",
              "bar" to "baz",
            )

          val result = webClient.requestList<StringModel>(HttpMethod.GET, getPath, headers = headers, UpstreamApi.TEST)
          mockServer.verify(
            getRequestedFor(urlEqualTo(getPath))
              .withHeader("foo", equalTo(headers["foo"]))
              .withHeader("bar", equalTo(headers["bar"])),
          )
          result.shouldBeInstanceOf<WebClientWrapperResponse.Success<List<StringModel>>>()
          result.data
            .first()
            .result
            .shouldBe("headers matched")
        }
      }
    }

    describe("when webClientWrapperResponse is Error") {
      it("returns an entity not found UpstreamApiError when the request 404's") {
        mockServer.stubGetTest(getPath, "", HttpStatus.NOT_FOUND)

        val result = webClient.request<TestModel>(HttpMethod.GET, getPath, headers, UpstreamApi.TEST)
        result.shouldBeInstanceOf<WebClientWrapperResponse.Error>()
        result.errors.shouldBe(
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              causedBy = UpstreamApi.TEST,
            ),
          ),
        )
      }

      it("returns a forbidden UpstreamApiError when the request 403's") {
        mockServer.stubGetTest(getPath, "", HttpStatus.FORBIDDEN)

        val result = webClient.request<TestModel>(HttpMethod.GET, getPath, headers, UpstreamApi.TEST, forbiddenAsError = true)
        result.shouldBeInstanceOf<WebClientWrapperResponse.Error>()
        result.errors.shouldBe(
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.FORBIDDEN,
              causedBy = UpstreamApi.TEST,
            ),
          ),
        )
      }

      it("returns a bad request UpstreamApiError when the request 400's") {
        mockServer.stubGetTest(getPath, "", HttpStatus.BAD_REQUEST)

        val result = webClient.request<TestModel>(HttpMethod.GET, getPath, headers, UpstreamApi.TEST, badRequestAsError = true)
        result.shouldBeInstanceOf<WebClientWrapperResponse.Error>()
        result.errors.shouldBe(
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.BAD_REQUEST,
              causedBy = UpstreamApi.TEST,
            ),
          ),
        )
      }

      it("throws an internal server error when the request 500's") {
        mockServer.stubGetTest(getPath, "", HttpStatus.INTERNAL_SERVER_ERROR)

        shouldThrow<WebClientResponseException.InternalServerError> {
          webClient.request<TestModel>(HttpMethod.GET, getPath, headers, UpstreamApi.TEST)
        }
      }

      it("throws a timeout error if response is too slow") {
        mockServer.stubGetTest(getPath, """{"result": "timeout"}""", delayMillis = 2000)

        val exception =
          shouldThrow<WebClientRequestException> {
            webClient.request<StringModel>(HttpMethod.GET, getPath, headers, UpstreamApi.TEST)
          }
        exception.cause?.javaClass?.simpleName shouldBe "ReadTimeoutException"
      }

      it("throws a connect timeout error if server is unreachable") {
        val timeoutWebClient =
          WebClientWrapper(
            "http://10.255.255.1:81",
            connectTimeoutMillis = 300,
            responseTimeoutSeconds = 2,
            featureFlagConfig,
          )

        val exception =
          shouldThrow<WebClientRequestException> {
            timeoutWebClient.request<StringModel>(
              HttpMethod.GET,
              "/test",
              headers,
              UpstreamApi.TEST,
            )
          }
        exception.cause?.javaClass?.simpleName shouldBe "ConnectTimeoutException"
      }
    }

    it("receives a very large response") {
      mockServer.stubGetTest(
        getPath,
        body = File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/extensions/fixtures/LargeResponse.json").readText(),
      )

      try {
        webClient.request<SearchModel>(HttpMethod.GET, getPath, headers = headers, UpstreamApi.TEST)
      } catch (_: WebClientResponseException) {
        fail("Exceeded memory buffer")
      }
    }
  })
