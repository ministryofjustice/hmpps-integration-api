package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.GenericApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.DataTransferObject.DataTransferObject
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person

data class TestDomainModel(val name: String)

class SimpleTestModel(val sourceName: String) {
  fun toTestDomainModel() = TestDomainModel(sourceName)
}

class ComplexTestModel(val sourceName: String) : DataTransferObject<TestDomainModel> {
  override fun toDomain() = TestDomainModel(sourceName)
}

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
    it("makes a simple get request") {
      val token = "4567"

      val webClient = WebClientWrapper(baseUrl = mockServer.baseUrl(), uri = "/test/$id", authToken = token)
      val person = webClient.simpleGet<SimpleTestModel>()
      val domainPerson = person?.toTestDomainModel() //conversion is performed manually

      domainPerson?.name.shouldBe("Harold")
    }

    it("makes a complex get request") {
      val token = "4567"

      val webClient = WebClientWrapper(baseUrl = mockServer.baseUrl(), uri = "/test/$id", authToken = token)
      val person = webClient.complexGet<ComplexTestModel, TestDomainModel>() //conversion is performed for us

      person?.name.shouldBe("Harold")
    }
  }
})
