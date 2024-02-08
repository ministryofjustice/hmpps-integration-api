package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.http.HttpStatus

class CaseNotesApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 4008
  }

  fun getCaseNotes(id: String, body: String, status: HttpStatus = HttpStatus.OK) {
    stubFor(
      WireMock.get("/case-notes/$id")
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
