package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.inspectors.shouldForAll
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.GenericApiMockServer

data class TestModel(val sourceName: String, val sourceLastName: String?) {
  fun toDomain() = TestDomainModel(sourceName, sourceLastName)
}

data class SearchModel(val content: List<TestModel>)

data class TestDomainModel(val firstName: String, val lastName: String?)

class WebClientWrapperTest : DescribeSpec({
  val mockServer = GenericApiMockServer()
  val id = "ABC1234"
  val token = "4567"

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
    val testModel = webClient.request<TestModel>(HttpMethod.GET, "/test/$id", token)
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
      token,
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
    val searchModel = webClient.request<SearchModel>(HttpMethod.POST, "/testPost", token, mapOf("sourceName" to "Paul"))
    val testDomainModels = searchModel.content.map { it.toDomain() }

    testDomainModels.shouldForAll { it.firstName.shouldBe("Paul") }
    testDomainModels.first().lastName.shouldBe("Paper")
    testDomainModels.last().lastName.shouldBe("Card")
  }
},)
