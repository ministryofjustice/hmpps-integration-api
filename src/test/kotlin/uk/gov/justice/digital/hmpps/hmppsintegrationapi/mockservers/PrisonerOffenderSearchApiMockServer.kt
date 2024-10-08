package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.matching
import com.github.tomakehurst.wiremock.client.WireMock.post
import org.springframework.http.HttpStatus

class PrisonerOffenderSearchApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 4001
  }

  fun stubGetPrisoner(
    nomisNumber: String,
    responseBody: String,
    status: HttpStatus = HttpStatus.OK,
  ) {
    stubFor(
      get("/prisoner/$nomisNumber")
        .withHeader("Authorization", matching("Bearer ${HmppsAuthMockServer.TOKEN}"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(status.value())
            .withBody(responseBody.trimIndent()),
        ),
    )
  }

  fun stubPostPrisonerSearch(
    requestBody: String,
    responseBody: String,
    status: HttpStatus = HttpStatus.OK,
  ) {
    stubFor(
      post("/global-search?size=9999")
        .withHeader("Authorization", matching("Bearer ${HmppsAuthMockServer.TOKEN}"))
        .withRequestBody(WireMock.equalToJson(requestBody))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(status.value())
            .withBody(responseBody.trimIndent()),
        ),
    )
  }
}
