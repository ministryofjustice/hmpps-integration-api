package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import io.kotest.assertions.fail
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.inspectors.shouldForAll
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.GenericApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File

data class StringModel(val headers: String)
data class TestModel(val sourceName: String, val sourceLastName: String?) {
  fun toDomain() = TestDomainModel(sourceName, sourceLastName)
}

data class SearchModel(val content: List<TestModel>)

data class TestDomainModel(val firstName: String, val lastName: String?)

class WebClientWrapperTest : DescribeSpec({
  val mockServer = GenericApiMockServer()
  val id = "ABC1234"
  val headers = mapOf("foo" to "bar")

  beforeEach() {
    mockServer.start()
  }

  afterTest() {
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

        val webClient = WebClientWrapper(baseUrl = mockServer.baseUrl())
        val result = webClient.request<TestModel>(HttpMethod.GET, "/test/$id", headers, UpstreamApi.TEST)

        if (result is WebClientWrapperResponse.Success) {
          val testDomainModel = result.data.toDomain()
          testDomainModel.firstName.shouldBe("Harold")
        }
      }

      it("performs a post request where the response is a json object") {
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

        val webClient = WebClientWrapper(baseUrl = mockServer.baseUrl())
        val result = webClient.request<SearchModel>(HttpMethod.POST, "/testPost", headers, UpstreamApi.TEST, mapOf("sourceName" to "Paul"))

        if (result is WebClientWrapperResponse.Success) {
          val testDomainModels = result.data.content.map { it.toDomain() }

          testDomainModels.shouldForAll { it.firstName.shouldBe("Paul") }
          testDomainModels.first().lastName.shouldBe("Paper")
          testDomainModels.last().lastName.shouldBe("Card")
        }
      }

      it("performs a request with multiple headers for .request()") {
        mockServer.stubGetWithHeadersTest()

        val headers = mapOf(
          "foo" to "bar",
          "bar" to "baz",
        )

        val webClient = WebClientWrapper(baseUrl = mockServer.baseUrl())
        val result = webClient.request<StringModel>(HttpMethod.GET, "/test", headers = headers, UpstreamApi.TEST)

        if (result is WebClientWrapperResponse.Success) {
          result.data.headers.shouldBe("headers matched")
        }
      }
    }

    describe("when requestList") {
      it("performs a GET request where the response is an array") {
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

        val webClient = WebClientWrapper(baseUrl = mockServer.baseUrl())
        val result = webClient.requestList<TestModel>(
          HttpMethod.GET,
          "/testPost",
          headers,
          UpstreamApi.TEST,
        )

        if (result is WebClientWrapperResponse.Success) {
          val testDomainModels = result.data.map { it.toDomain() }
          testDomainModels.shouldForAll { it.firstName.shouldBe("Paul") }
        }
      }

      it("performs a post request where the response is an array") {
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

        val webClient = WebClientWrapper(baseUrl = mockServer.baseUrl())
        val result = webClient.requestList<TestModel>(
          HttpMethod.POST,
          "/testPost",
          headers,
          UpstreamApi.TEST,
          mapOf("sourceName" to "Paul"),
        )

        if (result is WebClientWrapperResponse.Success) {
          val testDomainModels = result.data.map { it.toDomain() }
          testDomainModels.shouldForAll { it.firstName.shouldBe("Paul") }
        }
      }

      it("performs a request with multiple headers for .requestList()") {
        mockServer.stubGetWithHeadersTest()

        val headers = mapOf(
          "foo" to "bar",
          "bar" to "baz",
        )

        val webClient = WebClientWrapper(baseUrl = mockServer.baseUrl())
        val result = webClient.requestList<StringModel>(HttpMethod.GET, "/test", headers = headers, UpstreamApi.TEST)

        if (result is WebClientWrapperResponse.Success) {
          result.data.first().headers.shouldBe("headers matched")
        }
      }
    }
  }

  describe("when webClientWrapperResponse is Error") {
    it("returns an entity not found UpstreamApiError when the request 404's") {
      mockServer.stubPostTest("", HttpStatus.NOT_FOUND)
      val webClient = WebClientWrapper(baseUrl = mockServer.baseUrl())
      val result = webClient.request<TestModel>(HttpMethod.GET, "/test/$id", headers, UpstreamApi.TEST)

      if (result is WebClientWrapperResponse.Error) {
        result.errors.shouldBe(
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              causedBy = UpstreamApi.TEST,
            ),
          ),
        )
      }
    }

    it("returns a forbidden UpstreamApiError when the request 403's") {
      mockServer.stubPostTest("", HttpStatus.FORBIDDEN)
      val webClient = WebClientWrapper(baseUrl = mockServer.baseUrl())
      val result = webClient.request<TestModel>(HttpMethod.GET, "/test/$id", headers, UpstreamApi.TEST)

      if (result is WebClientWrapperResponse.Error) {
        result.errors.shouldBe(
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.FORBIDDEN,
              causedBy = UpstreamApi.TEST,
            ),
          ),
        )
      }
    }

    it("returns a bad request UpstreamApiError when the request 400's") {
      mockServer.stubPostTest("", HttpStatus.BAD_REQUEST)
      val webClient = WebClientWrapper(baseUrl = mockServer.baseUrl())
      val result = webClient.request<TestModel>(HttpMethod.GET, "/test/$id", headers, UpstreamApi.TEST)

      if (result is WebClientWrapperResponse.Error) {
        result.errors.shouldBe(
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.BAD_REQUEST,
              causedBy = UpstreamApi.TEST,
            ),
          ),
        )
      }
    }

    it("returns a internal server error UpstreamApiError when the request 500's") {
      mockServer.stubPostTest("", HttpStatus.INTERNAL_SERVER_ERROR)
      val webClient = WebClientWrapper(baseUrl = mockServer.baseUrl())
      val result = webClient.request<TestModel>(HttpMethod.GET, "/test/$id", headers, UpstreamApi.TEST)

      if (result is WebClientWrapperResponse.Error) {
        result.errors.shouldBe(
          listOf(
            UpstreamApiError(
              type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
              causedBy = UpstreamApi.TEST,
            ),
          ),
        )
      }
    }
  }

  it("receives a very large response") {
    val id = "A123"

    mockServer.stubGetTest(
      id = id,
      body = File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/extensions/fixtures/LargeResponse.json").readText(),
    )

    try {
      val webClient = WebClientWrapper(baseUrl = mockServer.baseUrl())
      webClient.request<SearchModel>(HttpMethod.GET, "/test/$id", headers = headers, UpstreamApi.TEST)
    } catch (e: WebClientResponseException) {
      fail("Exceeded memory buffer")
    }
  }
},)
