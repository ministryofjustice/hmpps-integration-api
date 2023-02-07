package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.matching

class PrisonerOffenderSearchApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 4001
  }

  fun stubGetPrisoner(prisonerId: String, body: String) {
    stubFor(
      get("/prisoner/$prisonerId")
        .withHeader(
          "Authorization", matching("Bearer ${HmppsAuthMockServer.TOKEN}")
        ).willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
            .withBody(body.trimIndent())
        )
    )
  }
}
