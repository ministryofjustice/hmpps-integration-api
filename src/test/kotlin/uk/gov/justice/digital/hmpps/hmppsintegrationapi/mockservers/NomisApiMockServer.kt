package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.matching
import com.github.tomakehurst.wiremock.client.WireMock.post
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
    path: String,
    reqBody: String,
    resBody: String,
    status: HttpStatus = HttpStatus.OK,
  ) {
    stubFor(
      post(path)
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
