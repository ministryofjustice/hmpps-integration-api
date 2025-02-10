package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.matching
import org.springframework.http.HttpStatus

class NonAssociationsApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    // check this port is valid in the case of other tests using it
    private const val WIREMOCK_PORT = 4005
  }

  fun stubNonAssociationsGet(
    prisonerNumber: String,
    includeOpen: String? = "true",
    includeClosed: String? = "false",
    body: String,
    status: HttpStatus = HttpStatus.OK,
  ) {
    stubFor(
      get("/prisoner/$prisonerNumber/non-associations?includeOpen=$includeOpen&includeClosed=$includeClosed")
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
