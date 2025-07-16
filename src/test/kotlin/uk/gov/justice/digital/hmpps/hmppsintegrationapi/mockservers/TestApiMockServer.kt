package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.http.HttpStatus

class TestApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 4000
  }

  fun stubGetTest(
    path: String,
    body: String,
    status: HttpStatus = HttpStatus.OK,
    delayMillis: Int = 0,
  ) {
    stubFor(
      WireMock
        .get(path)
        .willReturn(
          WireMock
            .aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(status.value())
            .withBody(body.trimIndent())
            .withFixedDelay(delayMillis),
        ),
    )
  }

  fun stubPostTest(
    path: String,
    body: String,
    status: HttpStatus = HttpStatus.OK,
    delayMillis: Int = 0,
  ) {
    stubFor(
      WireMock.post(path).willReturn(
        WireMock
          .aResponse()
          .withHeader("Content-Type", "application/json")
          .withStatus(status.value())
          .withBody(body.trimIndent())
          .withFixedDelay(delayMillis),
      ),
    )
  }
}
