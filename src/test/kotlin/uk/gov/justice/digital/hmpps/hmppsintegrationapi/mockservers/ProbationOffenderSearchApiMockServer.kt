package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.matching
import com.github.tomakehurst.wiremock.client.WireMock.post
import org.springframework.http.HttpStatus

class ProbationOffenderSearchApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 4002
  }

  fun stubPostOffenderSearch(requestBody: String, responseBody: String, status: HttpStatus = HttpStatus.OK) {
    stubFor(
      post("/search")
        .withHeader("Authorization", matching("Bearer ${HmppsAuthMockServer.TOKEN}"))
        .withRequestBody(equalToJson(requestBody))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(status.value())
            .withBody(responseBody.trimIndent())
        )
    )
  }
}
