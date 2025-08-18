package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.jsonwebtoken.Jwts
import java.time.Instant

class HmppsAuthMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    val TOKEN = "mock-bearer-token"
    private const val WIREMOCK_PORT = 3000
  }

  private val authUrl = "/auth/oauth/token?grant_type=client_credentials"

  fun getToken(expiresInMinutes: Long = 20): String {
    val key =
      Jwts.SIG.HS256
        .key()
        .build()
    val jwt =
      Jwts
        .builder()
        .claims(mapOf("client_id" to "test_client", "exp" to Instant.now().plusSeconds(60 * expiresInMinutes).epochSecond))
        .signWith(key)
        .compact()

    return jwt
  }

  fun stubGetOAuthToken(
    client: String,
    clientSecret: String,
    token: String = getToken(),
  ) {
    stubFor(
      WireMock
        .post(authUrl)
        .withBasicAuth(client, clientSecret)
        .willReturn(
          WireMock
            .aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
            .withBody(
              """
              {
                "access_token": "$token"
              }
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubServiceUnavailableForGetOAuthToken() {
    stubFor(
      WireMock
        .post(authUrl)
        .willReturn(
          WireMock.serviceUnavailable(),
        ),
    )
  }

  fun stubUnauthorizedForGetOAAuthToken() {
    stubFor(
      WireMock
        .post(authUrl)
        .willReturn(
          WireMock.unauthorized(),
        ),
    )
  }
}
