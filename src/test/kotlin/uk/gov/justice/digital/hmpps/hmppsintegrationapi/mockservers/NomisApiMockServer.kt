@file:Suppress("ktlint:standard:no-wildcard-imports")

package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.springframework.http.HttpStatus

class NomisApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 4000
  }

  fun stubNomisApiResponse(
    path: String,
    body: String,
    status: HttpStatus = HttpStatus.OK,
  ) {
    stubFor(
      get(path)
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

  fun stubNomisApiResponseForPost(
    prisonId: String,
    nomisNumber: String,
    reqBody: String,
    resBody: String,
    status: HttpStatus = HttpStatus.OK,
  ) {
    val externalUrl = urlPathTemplate("/api/v1/prison/$prisonId/offenders/$nomisNumber/transactions")

    stubFor(
      post(externalUrl)
        .withHeader(
          "Authorization",
          matching("Bearer ${HmppsAuthMockServer.TOKEN}"),
        ).withRequestBody(equalToJson(reqBody))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(status.value())
            .withBody(resBody.trimIndent()),
        ),
    )
  }

  fun stubGetImageData(
    imageId: Int,
    status: HttpStatus = HttpStatus.OK,
  ) {
    stubFor(
      get("/api/images/$imageId/data")
        .withHeader(
          "Authorization",
          matching("Bearer ${HmppsAuthMockServer.TOKEN}"),
        ).willReturn(
          aResponse()
            .withHeader("Content-Type", "image/jpeg")
            .withStatus(status.value())
            .withBodyFile("example.jpg"),
        ),
    )
  }
}
