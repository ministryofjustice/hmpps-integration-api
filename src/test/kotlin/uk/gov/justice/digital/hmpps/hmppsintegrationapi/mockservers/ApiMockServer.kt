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
      val apiMockerServerConfig =
        when (upstreamApi) {
          UpstreamApi.PRISONER_OFFENDER_SEARCH -> ApiMockServerConfig(4000)
          UpstreamApi.HEALTH_AND_MEDICATION -> ApiMockServerConfig(4001)
          UpstreamApi.MANAGE_POM_CASE -> ApiMockServerConfig(4002)
          UpstreamApi.PLP -> ApiMockServerConfig(4003)
          UpstreamApi.ACTIVITIES -> ApiMockServerConfig(4004)
          // USE PRISM
          UpstreamApi.PRISON_API -> ApiMockServerConfig(4000)
          UpstreamApi.NDELIUS -> ApiMockServerConfig(4003)
          UpstreamApi.ASSESS_RISKS_AND_NEEDS -> ApiMockServerConfig(4004)
          UpstreamApi.EFFECTIVE_PROPOSAL_FRAMEWORK -> ApiMockServerConfig(4005)
          UpstreamApi.ADJUDICATIONS -> ApiMockServerConfig(4006)
          UpstreamApi.CVL -> ApiMockServerConfig(4007)
          UpstreamApi.CASE_NOTES -> ApiMockServerConfig(4008)
          UpstreamApi.RISK_MANAGEMENT_PLAN -> ApiMockServerConfig(4004)
          UpstreamApi.TEST -> TODO()
          UpstreamApi.NON_ASSOCIATIONS -> ApiMockServerConfig(4005)
          UpstreamApi.PERSONAL_RELATIONSHIPS -> ApiMockServerConfig(4006)
          UpstreamApi.MANAGE_PRISON_VISITS -> ApiMockServerConfig(4007)
          UpstreamApi.INCENTIVES -> ApiMockServerConfig(4008)
          UpstreamApi.PRISONER_ALERTS -> ApiMockServerConfig(4009)
          UpstreamApi.LOCATIONS_INSIDE_PRISON -> ApiMockServerConfig(4000)
        }

      val wireMockConfig = WireMockConfiguration.wireMockConfig().port(apiMockerServerConfig.port)
      return ApiMockServer(wireMockConfig)
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
