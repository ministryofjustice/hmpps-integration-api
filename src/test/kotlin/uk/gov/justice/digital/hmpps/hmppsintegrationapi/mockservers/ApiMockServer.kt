package uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers

import com.atlassian.oai.validator.OpenApiInteractionValidator
import com.atlassian.oai.validator.whitelist.ValidationErrorsWhitelist
import com.atlassian.oai.validator.whitelist.rule.WhitelistRules
import com.atlassian.oai.validator.wiremock.OpenApiValidationListener
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.matching
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.stubbing.Scenario
import io.swagger.v3.parser.OpenAPIV3Parser
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
          UpstreamApi.PRISONER_OFFENDER_SEARCH -> ApiMockServerConfig(4000, "prisoner-search.json", true)
          UpstreamApi.HEALTH_AND_MEDICATION -> ApiMockServerConfig(4001, "health-and-medication.json")
          UpstreamApi.MANAGE_POM_CASE -> ApiMockServerConfig(4002, "manage-POM.json")
          UpstreamApi.PLP -> ApiMockServerConfig(4003, "plp.json")
          UpstreamApi.ACTIVITIES -> ApiMockServerConfig(4004, "activities.json")
          UpstreamApi.TEST -> ApiMockServerConfig(4005, "test.json")
          UpstreamApi.NDELIUS_INTEGRATION_TEST -> ApiMockServerConfig(4201)
          UpstreamApi.PRISONER_BASE_LOCATION -> ApiMockServerConfig(4024)
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
          UpstreamApi.SAN -> ApiMockServerConfig(4200)
        }

      val wireMockConfig = WireMockConfiguration.wireMockConfig().port(apiMockerServerConfig.port)

      if (apiMockerServerConfig.configPath != null) {
        val specPath = "src/test/resources/openapi-specs/${apiMockerServerConfig.configPath}"
        /*
          For schemas that use discriminators for inheritance, we must use the OpenAPIV3Parser to create the validator.
          We must also set the bind-type system property to true.
          This is to preserve the type of the discriminator, which is being overridden by the atlassian and swagger core libraries.
          The discriminator type is essential when we are using the swagger-core-request-validator
          For swagger core issue @see https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---OpenAPI-3.1#foreword
          We set the bind-type to true to preserve the type in version 3.1 and prevent it being nulled in favour of the types array introduced in 3.1.
          For atlassion code @see https://bitbucket.org/atlassian/swagger-request-validator/src/4e3e8fe412e99517a03e32a2c3217eb064c44823/swagger-request-validator-core/src/main/java/com/atlassian/oai/validator/util/OpenApiLoader.java?at=master#lines-55
          We use OpenAPIV3Parser instead of atlassions OpenAPILoader for schemas that use discriminators to ensure the request is fully validated.
         */
        val openApiInteractionValidator =
          if (apiMockerServerConfig.overrideBindType) {
            // Adding a whitelist to ignore discriminator validation as currently the prisoner search api spec is unable to provide the discriminator mappings
            val whitelist =
              ValidationErrorsWhitelist
                .create()
                .withRule(
                  "Ignore discriminator errors",
                  WhitelistRules.allOf(NestedMessagesValidationRule.nestedMessageHasKey("validation.request.body.schema.discriminator")),
                )
            withBindTypeSet {
              OpenApiInteractionValidator
                .Builder()
                .withWhitelist(whitelist)
                .withApi(OpenAPIV3Parser().readLocation(specPath, null, null).openAPI)
                .build()
            }
          } else {
            OpenApiInteractionValidator.createFor(specPath).build()
          }
        val validationListener = BindTypeValidationListener(openApiInteractionValidator, apiMockerServerConfig.overrideBindType)
        return ApiMockServer(wireMockConfig.extensions(ResetValidationEventListener(validationListener)), validationListener)
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

  fun assertValidationFailed() {
    this.validationListener?.report?.hasErrors()
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
          .withHeader(
            "Authorization",
            matching("Bearer ${HmppsAuthMockServer.TOKEN}"),
          ).inScenario(scenario)
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

inline fun <reified T> withBindTypeSet(block: () -> T): T {
  val bindType =
    try {
      System.getProperty("bind-type")
    } catch (e: NullPointerException) {
      null
    }
  if (bindType == null || bindType == "false") {
    System.setProperty("bind-type", "true")
  }
  val result = block.invoke()
  if (bindType == null) {
    System.clearProperty("bind-type")
  } else {
    System.setProperty("bind-type", bindType)
  }
  return result
}
