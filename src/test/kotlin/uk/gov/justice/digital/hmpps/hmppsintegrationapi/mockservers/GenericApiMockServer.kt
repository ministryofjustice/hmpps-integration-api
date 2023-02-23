package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import org.springframework.http.HttpStatus

class GenericApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 4000
  }

  fun stubGetTest(id: String, body: String, status: HttpStatus = HttpStatus.OK) {
    stubFor(
      WireMock.get("/test/$id").willReturn(
        WireMock.aResponse()
          .withHeader("Content-Type", "application/json")
          .withStatus(status.value())
          .withBody(body.trimIndent())
      )
    )
  }
}
