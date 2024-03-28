package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.http.HttpStatus

class CreateAndVaryLicenceApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 4007
  }

  fun stubGetLicenceSummaries(
    id: String,
    body: String,
    status: HttpStatus = HttpStatus.OK,
  ) {
    stubFor(
      WireMock.get("/public/licence-summaries/crn/$id")
        .withHeader(
          "Authorization",
          WireMock.matching("Bearer ${HmppsAuthMockServer.TOKEN}"),
        ).willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(status.value())
            .withBody(body.trimIndent()),
        ),
    )
  }

  fun stubGetLicenceConditions(
    id: String,
    body: String,
    status: HttpStatus = HttpStatus.OK,
  ) {
    stubFor(
      WireMock.get("/public/licences/id/$id")
        .withHeader(
          "Authorization",
          WireMock.matching("Bearer ${HmppsAuthMockServer.TOKEN}"),
        ).willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(status.value())
            .withBody(body.trimIndent()),
        ),
    )
  }
}
