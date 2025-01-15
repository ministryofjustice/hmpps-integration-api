package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.http.HttpStatus

class GenericApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 4000
  }

  fun stubGetWithHeadersTest() {
    stubFor(
      WireMock
        .get("/test")
        .withHeader("foo", WireMock.equalTo("bar"))
        .withHeader("bar", WireMock.equalTo("baz"))
        .willReturn(
          WireMock
            .aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody("""{"headers":"headers matched"}"""),
        ),
    )
  }

  fun stubGetTest(
    id: String,
    body: String,
    status: HttpStatus = HttpStatus.OK,
  ) {
    stubFor(
      WireMock
        .get("/test/$id")
        .willReturn(
          WireMock
            .aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(status.value())
            .withBody(body.trimIndent()),
        ),
    )
  }

  fun stubPostTest(
    body: String,
    status: HttpStatus = HttpStatus.OK,
  ) {
    stubFor(
      WireMock.post("/testPost").willReturn(
        WireMock
          .aResponse()
          .withHeader("Content-Type", "application/json")
          .withStatus(status.value())
          .withBody(body.trimIndent()),
      ),
    )
  }
}
