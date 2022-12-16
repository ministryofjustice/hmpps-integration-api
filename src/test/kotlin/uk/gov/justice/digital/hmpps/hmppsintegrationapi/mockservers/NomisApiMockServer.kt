package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get

class NomisApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8081
  }

  fun stubGetOffender(offenderNo: Int) {
    stubFor(
      get("/api/offenders/$offenderNo").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withStatus(200)
          .withBody(
            """
              { 
                "offenderNo": "$offenderNo",
                "firstName": "John",
                "lastName": "Smith"
              }
              """.trimIndent()
          )
      )
    )
  }
}
