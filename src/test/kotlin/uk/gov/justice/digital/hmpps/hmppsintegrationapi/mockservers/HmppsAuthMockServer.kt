package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock

class HmppsAuthMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    val TOKEN = "bearer-token"
    private const val WIREMOCK_PORT = 3000
  }

  fun stubGetOauthToken(client: String, clientSecret: String) {
    stubFor(
      WireMock.post("/auth/oauth/token?grant_type=client_credentials")
        .withBasicAuth(client, clientSecret)
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
            .withBody(
              """
                { 
                  "access_token": "$TOKEN"
                }
              """.trimIndent()
            )
        )
    )
  }
}
