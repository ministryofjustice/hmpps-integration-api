package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.matching
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi

class ApiMockServer(
  config: WireMockConfiguration,
) : WireMockServer(config) {
  companion object {
    // These ports must match the config in the yaml files
    fun create(upstreamApi: UpstreamApi): ApiMockServer {
      val port =
        when (upstreamApi) {
          UpstreamApi.PRISONER_OFFENDER_SEARCH -> 4000
          UpstreamApi.HEALTH_AND_MEDICATION -> 4001
          UpstreamApi.MANAGE_POM_CASE -> 4002
          UpstreamApi.PLP -> 4003
          UpstreamApi.ACTIVITIES -> 4004
          // USE PRISM
          UpstreamApi.PRISON_API -> 4000
          UpstreamApi.NDELIUS -> 4003
          UpstreamApi.ASSESS_RISKS_AND_NEEDS -> 4004
          UpstreamApi.EFFECTIVE_PROPOSAL_FRAMEWORK -> 4005
          UpstreamApi.ADJUDICATIONS -> 4006
          UpstreamApi.CVL -> 4007
          UpstreamApi.CASE_NOTES -> 4008
          UpstreamApi.RISK_MANAGEMENT_PLAN -> 4004
          UpstreamApi.TEST -> TODO()
          UpstreamApi.NON_ASSOCIATIONS -> 4005
          UpstreamApi.PERSONAL_RELATIONSHIPS -> 4006
          UpstreamApi.MANAGE_PRISON_VISITS -> 4007
          UpstreamApi.INCENTIVES -> 4008
          UpstreamApi.PRISONER_ALERTS -> 4009
          UpstreamApi.LOCATIONS_INSIDE_PRISON -> 4000
        }

      val config = WireMockConfiguration.wireMockConfig().port(port)
      return ApiMockServer(config)
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

  fun stubForPost(
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

  fun stubForImageData(
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
