package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.matching
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi

class ApiMockServer(
  private val port: Int,
) : WireMockServer(port) {
  companion object {
    fun create(upstreamApi: UpstreamApi): ApiMockServer =
      when (upstreamApi) {
        UpstreamApi.NOMIS -> ApiMockServer(4000)
        UpstreamApi.PRISONER_OFFENDER_SEARCH -> ApiMockServer(4001)
        UpstreamApi.PROBATION_OFFENDER_SEARCH -> ApiMockServer(4002)
        UpstreamApi.NDELIUS -> ApiMockServer(4003)
        UpstreamApi.ASSESS_RISKS_AND_NEEDS -> ApiMockServer(4004)
        UpstreamApi.ADJUDICATIONS -> ApiMockServer(4006)
        UpstreamApi.CVL -> ApiMockServer(4007)
        UpstreamApi.CASE_NOTES -> ApiMockServer(4008)
        UpstreamApi.MANAGE_POM_CASE -> ApiMockServer(4009)
        UpstreamApi.RISK_MANAGEMENT_PLAN -> ApiMockServer(4004)
        UpstreamApi.TEST -> TODO()
        UpstreamApi.PLP -> ApiMockServer(4004)
        UpstreamApi.NON_ASSOCIATIONS -> ApiMockServer(4005)
        UpstreamApi.PERSONAL_RELATIONSHIPS -> ApiMockServer(4006)
        UpstreamApi.MANAGE_PRISON_VISITS -> ApiMockServer(4007)
        UpstreamApi.INCENTIVES -> ApiMockServer(4008)
      }
  }

  fun stubForGet(
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
}
