package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.stubbing.Scenario
import org.springframework.http.HttpStatus

class TestApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 4000
  }

  fun stubGetTest(
    path: String,
    body: String,
    status: HttpStatus = HttpStatus.OK,
    delayMillis: Int = 0,
  ) {
    stubFor(
      WireMock
        .get(path)
        .willReturn(
          WireMock
            .aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(status.value())
            .withBody(body.trimIndent())
            .withFixedDelay(delayMillis),
        ),
    )
  }

  fun stubForRetry(
    scenario: String,
    path: String,
    numberOfRequests: Int = 4,
    failedStatus: Int,
    endStatus: Int,
    body: String,
  ) {
    (1..numberOfRequests).forEach {
      stubFor(
        get(path)
          .inScenario(scenario)
          .whenScenarioStateIs(if (it == 1) Scenario.STARTED else "RETRY${it - 1}")
          .willReturn(
            aResponse()
              .withHeader("Content-Type", "application/json")
              .withStatus(if (it == numberOfRequests) endStatus else failedStatus)
              .withBody(if (it == numberOfRequests) body else "Failed"),
          ).willSetStateTo("RETRY$it"),
      )
    }
  }

  fun stubPostTest(
    path: String,
    body: String,
    status: HttpStatus = HttpStatus.OK,
    delayMillis: Int = 0,
  ) {
    stubFor(
      WireMock.post(path).willReturn(
        WireMock
          .aResponse()
          .withHeader("Content-Type", "application/json")
          .withStatus(status.value())
          .withBody(body.trimIndent())
          .withFixedDelay(delayMillis),
      ),
    )
  }
}
