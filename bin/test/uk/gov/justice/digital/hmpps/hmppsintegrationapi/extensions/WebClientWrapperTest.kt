package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import io.kotest.assertions.fail
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.inspectors.shouldForAll
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.GenericApiMockServer
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

  it("performs a request where the result is a json object") {
    mockServer.stubGetTest(
      id,
      """
          {
            "sourceName" : "Harold"
          }
          """.removeWhitespaceAndNewlines(),
    )

    val webClient = WebClientWrapper(baseUrl = mockServer.baseUrl())
    val testModel = webClient.request<TestModel>(HttpMethod.GET, "/test/$id", headers)
    val testDomainModel = testModel.toDomain()

    testDomainModel?.firstName.shouldBe("Harold")
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
    val testModels = webClient.requestList<TestModel>(
      HttpMethod.POST,
      "/testPost",
      headers,
      mapOf("sourceName" to "Paul"),
    )

    val testDomainModels = testModels.map { it.toDomain() }

    testDomainModels.shouldForAll { it.firstName.shouldBe("Paul") }
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
    val searchModel = webClient.request<SearchModel>(HttpMethod.POST, "/testPost", headers, mapOf("sourceName" to "Paul"))
    val testDomainModels = searchModel.content.map { it.toDomain() }

    testDomainModels.shouldForAll { it.firstName.shouldBe("Paul") }
    testDomainModels.first().lastName.shouldBe("Paper")
    testDomainModels.last().lastName.shouldBe("Card")
  }

  it("performs a request with multiple headers for .request()") {
    mockServer.stubGetWithHeadersTest()

    val headers = mapOf(
      "foo" to "bar",
      "bar" to "baz",
    )

    val webClient = WebClientWrapper(baseUrl = mockServer.baseUrl())
    val result = webClient.request<StringModel>(HttpMethod.GET, "/test", headers = headers)

    result.headers.shouldBe("headers matched")
  }

  it("performs a request with multiple headers for .requestList()") {
    mockServer.stubGetWithHeadersTest()

    val headers = mapOf(
      "foo" to "bar",
      "bar" to "baz",
    )

    val webClient = WebClientWrapper(baseUrl = mockServer.baseUrl())
    val result = webClient.requestList<StringModel>(HttpMethod.GET, "/test", headers = headers)

    result.first().headers.shouldBe("headers matched")
  }

  it("receives a very large response") {
    val id = "A123"

    mockServer.stubGetTest(
      id = id,
      body = File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/extensions/fixtures/LargeResponse.json").readText(),
    )

    try {
      val webClient = WebClientWrapper(baseUrl = mockServer.baseUrl())
      webClient.request<SearchModel>(HttpMethod.GET, "/test/$id", headers = headers)
    } catch (e: WebClientResponseException) {
      fail("Exceeded memory buffer")
    }
  }
},)
