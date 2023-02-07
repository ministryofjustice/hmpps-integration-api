package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*

class ProbationOffenderSearchApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 4002
  }

  fun stubGetOffenderSearch(requestBody: String, responseBody: String) {
    stubFor(
      get("/search")
        .withHeader(
          "Authorization",
          matching("Bearer ${HmppsAuthMockServer.TOKEN}")
        )
        .withRequestBody(equalToJson(requestBody))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
            .withBody(responseBody.trimIndent())
        )
    )
  }
}
