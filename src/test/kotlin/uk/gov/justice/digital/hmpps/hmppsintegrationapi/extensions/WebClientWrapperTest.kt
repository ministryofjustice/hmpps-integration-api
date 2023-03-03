package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.inspectors.shouldForAll
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.GenericApiMockServer
data class TestDomainModel(val firstName: String)

data class TestModel(val sourceName: String) {
  fun toDomain() = TestDomainModel(sourceName)
}


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

  it("performs a get request") {
    mockServer.stubGetTest(
      id,
        """
          {
            "sourceName" : "Harold"
          }
          """.removeWhitespaceAndNewlines()
    )

    val webClient = WebClientWrapper(baseUrl = mockServer.baseUrl(), authToken = token)

    val testModel = webClient.get<TestModel>("/test/$id")
    val testDomainModel = testModel?.toDomain()

    testDomainModel?.firstName.shouldBe("Harold")
  }

  it ("performs a post request where the response is an array"){
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
      """.removeWhitespaceAndNewlines()
    )

    val webClient = WebClientWrapper(baseUrl = mockServer.baseUrl(), authToken = token)
    val testModel = webClient.post<TestModel>("/testPost", mapOf("sourceName" to "Paul"))
    val testDomainModels = testModel.map { it.toDomain() }

    testDomainModels.shouldForAll { it.firstName.shouldBe("Paul") }
  }

  it ("performs a post request where the response is a json object"){
    mockServer.stubPostTest(
      """
        {
          "content":
          [
            {
              "sourceName": "Paul"
            },
            {
              "sourceName": "Paul"
            }
          ]
        }
      """.removeWhitespaceAndNewlines()
    )

    val webClient = WebClientWrapper(baseUrl = mockServer.baseUrl(), authToken = token)
    val testModel = webClient.post<TestModel>("/testPost", mapOf("sourceName" to "Paul"))
    val testDomainModels = testModel.map { it.toDomain() }

    testDomainModels.shouldForAll { it.firstName.shouldBe("Paul") }
  }
})
