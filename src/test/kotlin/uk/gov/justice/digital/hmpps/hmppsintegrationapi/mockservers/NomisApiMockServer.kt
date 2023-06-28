package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.matching
import org.springframework.http.HttpStatus

class NomisApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 4000
  }

  fun stubGetOffender(offenderNo: String, body: String, status: HttpStatus = HttpStatus.OK) {
    stubFor(
      get("/api/offenders/$offenderNo")
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

  fun stubGetOffenderImageDetails(offenderNo: String, body: String, status: HttpStatus = HttpStatus.OK) {
    stubFor(
      get("/api/images/offenders/$offenderNo")
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

  fun stubGetImageData(imageId: Int, status: HttpStatus = HttpStatus.OK) {
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

  fun stubGetOffenderAddresses(offenderNo: String, body: String, status: HttpStatus = HttpStatus.OK) {
    stubFor(
      get("/api/offenders/$offenderNo/addresses")
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

  fun stubGetConvictionsForPerson(offenderNo: String, body: String, status: HttpStatus = HttpStatus.OK) {
    stubFor(
      get("/api/bookings/offenderNo/$offenderNo/offenceHistory")
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
}
