package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.GenericApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person

class TestModel(val sourceName: String) {
  // fun toTestDomainModel() => TestDomainModel(sourceName)
}

class TestDomainModel(val name: String)

class WebClientWrapperTest : DescribeSpec({
  val mockServer = GenericApiMockServer()
  val id = "ABC1234"

  beforeTest() {
    mockServer.stubGetTest(
      id,
      """
        {
          "sourceName" : "Harold"
        }
        """
    )
  }

  beforeEach() {
    mockServer.start()
  }

  afterTest() {
    mockServer.stop()
  }

  describe("#makeWebClientRequest") {
    it("makes a get request") {
      val token = "4567"

      val webClient = WebClientWrapper(baseUrl = mockServer.baseUrl(), uri = "/test/$id", authToken = token)
      val person = webClient.get<Person>()

      person?.firstName.shouldBe("Frank")
    }
  }
})
