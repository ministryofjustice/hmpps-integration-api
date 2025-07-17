package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.atlassian.oai.validator.wiremock.OpenApiValidationListener
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
  private val validationListener: OpenApiValidationListener? = null,
) : WireMockServer(config) {
  companion object {
    // These ports must match the config in the yaml files
    fun create(upstreamApi: UpstreamApi): ApiMockServer {
      val apiMockerServerConfig =
        when (upstreamApi) {
          UpstreamApi.PRISONER_OFFENDER_SEARCH -> ApiMockServerConfig(4000)
          UpstreamApi.HEALTH_AND_MEDICATION -> ApiMockServerConfig(4001, "health-and-medication.json")
          UpstreamApi.MANAGE_POM_CASE -> ApiMockServerConfig(4002)
          UpstreamApi.PLP -> ApiMockServerConfig(4003)
          UpstreamApi.ACTIVITIES -> ApiMockServerConfig(4004, "activities.json")
          UpstreamApi.TEST -> ApiMockServerConfig(4005, "test.json")
          // USE PRISM
          UpstreamApi.PRISON_API -> ApiMockServerConfig(4000)
          UpstreamApi.NDELIUS -> ApiMockServerConfig(4003)
          UpstreamApi.ASSESS_RISKS_AND_NEEDS -> ApiMockServerConfig(4004)
          UpstreamApi.EFFECTIVE_PROPOSAL_FRAMEWORK -> ApiMockServerConfig(4005)
          UpstreamApi.ADJUDICATIONS -> ApiMockServerConfig(4006)
          UpstreamApi.CVL -> ApiMockServerConfig(4007)
          UpstreamApi.CASE_NOTES -> ApiMockServerConfig(4008)
          UpstreamApi.RISK_MANAGEMENT_PLAN -> ApiMockServerConfig(4004)
          UpstreamApi.NON_ASSOCIATIONS -> ApiMockServerConfig(4005)
          UpstreamApi.PERSONAL_RELATIONSHIPS -> ApiMockServerConfig(4006)
          UpstreamApi.MANAGE_PRISON_VISITS -> ApiMockServerConfig(4007)
          UpstreamApi.INCENTIVES -> ApiMockServerConfig(4008)
          UpstreamApi.PRISONER_ALERTS -> ApiMockServerConfig(4009)
          UpstreamApi.LOCATIONS_INSIDE_PRISON -> ApiMockServerConfig(4000)
        }

      val wireMockConfig = WireMockConfiguration.wireMockConfig().port(apiMockerServerConfig.port)

      if (apiMockerServerConfig.configPath != null) {
        val specPath = "src/test/resources/openapi-specs/${apiMockerServerConfig.configPath}"
        val validationListener = OpenApiValidationListener(specPath)
        return ApiMockServer(wireMockConfig, validationListener)
      }

      return ApiMockServer(wireMockConfig)
    }
  }

  init {
    if (validationListener != null) {
      super.addMockServiceRequestListener(validationListener)
    }
  }

  fun resetValidator() {
    this.validationListener?.reset()
  }

  fun assertValidationPassed() {
    this.validationListener?.assertValidationPassed()
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
