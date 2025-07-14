package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.atlassian.oai.validator.wiremock.OpenApiValidationListener
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import org.springframework.http.HttpMethod
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import java.io.File

class ApiMockServerTest :
  DescribeSpec({
    val mockServer = ApiMockServer.create(UpstreamApi.TEST)
    lateinit var webClient: WebClientWrapper

    beforeEach {
      mockServer.start()
      webClient = WebClientWrapper(mockServer.baseUrl())
    }

    afterEach {
      mockServer.stop()
      mockServer.resetValidator()
    }

    fun authenticationHeader(): Map<String, String> =
      mapOf(
        "Authorization" to "Bearer mock-bearer-token",
      )

    it("correctly validates against spec") {
      mockServer.stubForGet(
        "/pet/1",
        File("src/test/resources/fixtures/test/GetPetById.json").readText(),
      )

      webClient.request<Any>(HttpMethod.GET, "/pet/1", authenticationHeader(), UpstreamApi.TEST)

      mockServer.assertValidationPassed()
    }

    it("correctly fails to validate against spec") {
      mockServer.stubForGet(
        "/pet/1",
        File("src/test/resources/fixtures/test/GetPetByIdIncorrect.json").readText(),
      )

      webClient.request<Any>(HttpMethod.GET, "/pet/1", authenticationHeader(), UpstreamApi.TEST)

      shouldThrow<OpenApiValidationListener.OpenApiValidationException> {
        mockServer.assertValidationPassed()
      }
    }
  })
