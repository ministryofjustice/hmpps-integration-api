package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.GenericApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.DataTransferObject.DataTransferObject

data class TestDomainModel(val name: String)

class TestModel(val sourceName: String) : DataTransferObject<TestDomainModel> {
  override fun toDomain() = TestDomainModel(sourceName)
}

class WebClientWrapperTest : DescribeSpec({
  val mockServer = GenericApiMockServer()
  val id = "ABC1234"
  val token = "4567"

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

  it("returns a person") {
    val webClient = WebClientWrapper(baseUrl = mockServer.baseUrl(), authToken = token)

    val person = webClient.getOne<TestModel, TestDomainModel>("/test/$id")

    person.shouldBeTypeOf<TestDomainModel>()
    person?.name.shouldBe("Harold")
  }

  it("returns a 404 not found") {
    val webClient = WebClientWrapper(baseUrl = mockServer.baseUrl(), authToken = token)

    shouldThrow<WebClientResponseException.NotFound> {
      webClient.getOne<TestModel, TestDomainModel>("/test/$id")
    }
  }
})
