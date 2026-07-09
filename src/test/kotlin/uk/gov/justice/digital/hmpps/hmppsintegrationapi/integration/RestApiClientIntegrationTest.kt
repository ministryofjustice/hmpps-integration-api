package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.TestApiMockServer

class RestApiClientIntegrationTest :
  DescribeSpec({
    // Use WireMock for testing the RestApiClient
    // then we can mock the RestApiClient for testing gateways.
    val mockServer = TestApiMockServer()

    beforeEach {
      mockServer.start()
      mockServer.resetAll()
    }

    afterTest {
      mockServer.stop()
    }

    describe("basic rest client functionality") {
      it("Should handle a basic GET request") {
        mockServer.stubGetTest(
          "/api/1234",
          """
              {
                "name": "Tester 123"
              }
            """.removeWhitespaceAndNewlines(),
          HttpStatus.OK,
        )
        val client = RestApiClient("Test API", mockServer.baseUrl())

        val response = client.get("/api/1234", TestItem::class)

        response.apiName shouldBe "Test API"
        response.status shouldBe HttpStatus.OK
        response.errors.size shouldBe 0
        response.data!!.name shouldBe "Tester 123"
      }

      it("Should handle not-found errors") {
        mockServer.stubGetTest(
          "/api/4567",
          "",
          HttpStatus.NOT_FOUND,
        )
        val client = RestApiClient("Test API", mockServer.baseUrl())

        val response = client.get("/api/4567", TestItem::class)

        response.apiName shouldBe "Test API"
        response.status shouldBe HttpStatus.NOT_FOUND
      }

      it("Can be used in a Gateway") {
        mockServer.stubGetTest(
          "/api/1234",
          """
              {
                "name": "Tester 1234"
              }
            """.removeWhitespaceAndNewlines(),
          HttpStatus.OK,
        )
        val gateway = TestGateway(mockServer.baseUrl())

        val response = gateway.restApiCall("1234")

        response.status shouldBe HttpStatus.OK
        response.data!!.name shouldBe "Tester 1234"
      }

      it("Should be easy to mock") {
        val mockClient: RestApiClient = mock()
        val gateway = TestGateway("http://localhost/", mockClient)
        val upstreamData = TestItem(name = "Tester 3456")
        whenever(mockClient.get("/api/3456", upstreamData::class)).thenReturn(RestApiResponse("", HttpStatus.OK, upstreamData))

        val response = gateway.restApiCall("3456")

        response.status shouldBe HttpStatus.OK
        response.data!!.name shouldBe "Tester 3456"
      }
    }
  })

data class TestItem(
  val name: String?,
)

class TestGateway(
  val baseUrl: String,
  val restApiClient: RestApiClient? = null,
) {
  fun restApiCall(id: String): RestApiResponse<TestItem> = restClient().get("/api/$id", TestItem::class)

  // Is there a way we can get Spring to inject a configured RestApiClient into the gateway?
  private fun restClient() = restApiClient ?: RestApiClient("TestApi", baseUrl)
}
