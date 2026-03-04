package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.servers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock

class PrisonerSearchMockServer internal constructor() : WireMockServer(8446) {
  fun stubGetPrisoner(
    nomsNumber: String,
    prisonId: String = "MDI",
  ) {
    stubFor(
      WireMock
        .get(WireMock.urlEqualTo("/prisoner/$nomsNumber"))
        .willReturn(
          WireMock
            .aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
              {
                "prisonerNumber": "$nomsNumber",
                "firstName": "Jane",
                "lastName": "Smith",
                "prisonId": "$prisonId"
              }
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubGetPrisonerNullPrisonId(nomsNumber: String) {
    stubFor(
      WireMock
        .get(WireMock.urlEqualTo("/prisoner/$nomsNumber"))
        .willReturn(
          WireMock
            .aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
              {
                "prisonerNumber": "$nomsNumber",
                "firstName": "Jane",
                "lastName": "Smith"
              }
              """.trimIndent(),
            ),
        ),
    )
  }
}
