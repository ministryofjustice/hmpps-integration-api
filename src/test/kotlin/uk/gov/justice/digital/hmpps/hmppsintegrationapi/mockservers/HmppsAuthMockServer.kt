package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock

class HmppsAuthMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    val TOKEN = "bearer-token"
    private const val WIREMOCK_PORT = 3000
  }

  private val authUrl = "/auth/oauth/token?grant_type=client_credentials"

  fun stubGetSuccessOAuthToken(client: String, clientSecret: String) {
    stubFor(
      WireMock.post(authUrl)
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

  fun stubGetConnectExceptionOAuthToken() {
    stubFor(
      WireMock.post(authUrl)
        .willReturn(
          WireMock.serviceUnavailable()
        )
    )
  }
}
