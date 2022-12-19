package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import java.util.*
import kotlin.reflect.full.companionObject

class NomisApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8081
  }

  fun stubGetOffender(offenderNo: String) {
    stubFor(
      get("/api/offenders/$offenderNo")
        .withHeader(
          "Authorization", matching("Bearer ${HmppsAuthMockServer.TOKEN}")
        ).willReturn(
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
