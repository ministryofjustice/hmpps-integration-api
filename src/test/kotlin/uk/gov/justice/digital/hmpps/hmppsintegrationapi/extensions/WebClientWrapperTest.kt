package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.kotest.assertions.fail
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.inspectors.shouldForAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.TestApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File
import kotlin.test.assertTrue

data class StringModel(
  val headers: String,
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

    val id = "ABC1234"
    val headers = mapOf("foo" to "bar")

    beforeEach {
      mockServer.start()
      webClient = WebClientWrapper(baseUrl = mockServer.baseUrl())
    }

    afterTest {
      mockServer.stop()
    }

    describe("when webClientWrapperResponse is Success") {
      describe("when request") {
        it("performs a GET request where the result is a json object") {
          mockServer.stubGetTest(
            id,
            """
            {
              "sourceName" : "Harold"
            }
            """.removeWhitespaceAndNewlines(),
          )

          val result = webClient.request<TestModel>(HttpMethod.GET, "/test/$id", headers, UpstreamApi.TEST)
          result.shouldBeInstanceOf<WebClientWrapperResponse.Success<TestModel>>()
          val testDomainModel = result.data.toDomain()
          testDomainModel.firstName.shouldBe("Harold")
        }

        it("performs a POST request where the response is a json object") {
          mockServer.stubPostTest(
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

          val result = webClient.request<SearchModel>(HttpMethod.POST, "/testPost", headers, UpstreamApi.TEST, mapOf("sourceName" to "Paul"))
          result.shouldBeInstanceOf<WebClientWrapperResponse.Success<SearchModel>>()
          val testDomainModels = result.data.content.map { it.toDomain() }
          testDomainModels.shouldForAll { it.firstName.shouldBe("Paul") }
          testDomainModels.first().lastName.shouldBe("Paper")
          testDomainModels.last().lastName.shouldBe("Card")
        }

        it("performs a POST request where the request body is an array") {
          mockServer.stubPostTest("{}")

          val result =
            webClient.request<Any>(
              HttpMethod.POST,
              "/testPost",
              headers,
              UpstreamApi.TEST,
              listOf("Paul"),
            )

          mockServer.verify(
            postRequestedFor(urlEqualTo("/testPost"))
              .withRequestBody(equalToJson("[\"Paul\"]"))
              .withHeader("Content-Type", equalTo("application/json")),
          )

          assertTrue { result is WebClientWrapperResponse.Success }
        }

        it("performs a GET request with multiple headers") {
          mockServer.stubGetWithHeadersTest()

          val headers =
            mapOf(
              "foo" to "bar",
              "bar" to "baz",
            )

          val result = webClient.request<StringModel>(HttpMethod.GET, "/test", headers = headers, UpstreamApi.TEST)
          result.shouldBeInstanceOf<WebClientWrapperResponse.Success<StringModel>>()
          result.data.headers.shouldBe("headers matched")
        }
      }

      describe("when requestList") {
        it("performs a GET request where the response is an array") {
          mockServer.stubGetTest(
            id,
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
              "/test/$id",
              headers,
              UpstreamApi.TEST,
            )
          result.shouldBeInstanceOf<WebClientWrapperResponse.Success<List<TestModel>>>()
          val testDomainModels = result.data.map { it.toDomain() }
          testDomainModels.shouldForAll { it.firstName.shouldBe("Paul") }
        }

        it("performs a POST request where the response is an array") {
          mockServer.stubPostTest(
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
              "/testPost",
              headers,
              UpstreamApi.TEST,
              mapOf("sourceName" to "Paul"),
            )
          result.shouldBeInstanceOf<WebClientWrapperResponse.Success<List<TestModel>>>()
          val testDomainModels = result.data.map { it.toDomain() }
          testDomainModels.shouldForAll { it.firstName.shouldBe("Paul") }
        }

        it("performs a POST request where the request body is an array") {
          mockServer.stubPostTest("{}")

          val result =
            webClient.requestList<Any>(
              HttpMethod.POST,
              "/testPost",
              headers,
              UpstreamApi.TEST,
              listOf("Paul"),
            )

          mockServer.verify(
            postRequestedFor(urlEqualTo("/testPost"))
              .withRequestBody(equalToJson("[\"Paul\"]"))
              .withHeader("Content-Type", equalTo("application/json")),
          )

          assertTrue { result is WebClientWrapperResponse.Success }
        }

        it("performs a GET request with multiple headers") {
          mockServer.stubGetWithHeadersTest()

          val headers =
            mapOf(
              "foo" to "bar",
              "bar" to "baz",
            )

          val result = webClient.requestList<StringModel>(HttpMethod.GET, "/test", headers = headers, UpstreamApi.TEST)
          result.shouldBeInstanceOf<WebClientWrapperResponse.Success<List<StringModel>>>()
          result.data
            .first()
            .headers
            .shouldBe("headers matched")
        }
      }
    }

    describe("when webClientWrapperResponse is Error") {
      it("returns an entity not found UpstreamApiError when the request 404's") {
        mockServer.stubGetTest(id, "", HttpStatus.NOT_FOUND)

        val result = webClient.request<TestModel>(HttpMethod.GET, "/test/$id", headers, UpstreamApi.TEST)
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
        mockServer.stubGetTest(id, "", HttpStatus.FORBIDDEN)

        val result = webClient.request<TestModel>(HttpMethod.GET, "/test/$id", headers, UpstreamApi.TEST, forbiddenAsError = true)
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
        mockServer.stubGetTest(id, "", HttpStatus.BAD_REQUEST)

        val result = webClient.request<TestModel>(HttpMethod.GET, "/test/$id", headers, UpstreamApi.TEST, badRequestAsError = true)
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
        mockServer.stubGetTest(id, "", HttpStatus.INTERNAL_SERVER_ERROR)

        shouldThrow<WebClientResponseException.InternalServerError> {
          webClient.request<TestModel>(HttpMethod.GET, "/test/$id", headers, UpstreamApi.TEST)
        }
      }
    }

    it("receives a very large response") {
      mockServer.stubGetTest(
        id = id,
        body = File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/extensions/fixtures/LargeResponse.json").readText(),
      )

      try {
        webClient.request<SearchModel>(HttpMethod.GET, "/test/$id", headers = headers, UpstreamApi.TEST)
      } catch (_: WebClientResponseException) {
        fail("Exceeded memory buffer")
      }
    }
  })
