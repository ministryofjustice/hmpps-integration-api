package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.matching
import org.springframework.http.HttpStatus

class ApiMockServer(
  private val port: Int,
) : WireMockServer(port) {
  fun stubForGet(
    path: String,
    body: String,
    status: HttpStatus = HttpStatus.OK,
  ) {
    stubFor(
      get(path)
        .withHeader(
          "Authorization",
          matching("Bearer ${HmppsAuthMockServer.TOKEN}"),
        ).willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(status.value())
            .withBody(body.trimIndent()),
        ),
    )
  }
}
