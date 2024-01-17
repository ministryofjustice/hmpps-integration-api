package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.http.HttpStatus

class AdjudicationsApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 4006
    private const val ACTIVE_CASELOAD = "MDI"
  }

  fun stubGetReportedAdjudicationsForPerson(id: String, body: String, status: HttpStatus = HttpStatus.OK) {
    stubFor(
      WireMock.get("/reported-adjudications/prisoner/$id")
        .withHeader(
          "Authorization",
          WireMock.matching("Bearer ${HmppsAuthMockServer.TOKEN}"),
        )
        .withHeader(
          "Active-Caseload",
          WireMock.equalTo(ACTIVE_CASELOAD),
        )
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(status.value())
            .withBody(body.trimIndent()),
        ),
    )
  }
}
