package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Court
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi

@Component
class CourtRegisterGateway(
  @Value("\${services.court-register.base-url}") val baseUrl: String,
  val features: FeatureFlagConfig = FeatureFlagConfig(),
  val courtRegisterRestClient: RestApiClient? = null,
) : UpstreamGateway {
  override fun metaData() =
    GatewayMetadata(
      summary = "This is a standardised list of court data that can be shared between courts and prisons",
      developerPortalId = "DPS109",
      developerPortalUrl = "https://developer-portal.hmpps.service.justice.gov.uk/products/court-register-prisons",
      apiDocUrl = "https://court-register-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.htm",
      apiSpecUrl = "https://court-register-api-dev.hmpps.service.justice.gov.uk/v3/api-docs",
      gitHubRepoUrl = "https://github.com/ministryofjustice/hmpps-court-register-api",
      slackChannel = "#calculate_release_dates_public_channel",
    )

  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getCourt(
    courtId: String,
    requestContext: RequestContext?,
  ): Response<Court?> {
    if (useRestApiClient()) {
      return getCourtWithRestClient(courtId, requestContext)
    }

    val result =
      webClient.request<Court>(
        HttpMethod.GET,
        "/courts/id/$courtId",
        authenticationHeader(requestContext),
        UpstreamApi.COURT_REGISTER,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        val court = result.data
        Response(data = court)
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }

  fun getCourtWithRestClient(
    courtId: String,
    requestContext: RequestContext?,
  ): Response<Court?> {
    val result =
      courtRegisterRestClient!!.get(
        "/courts/id/$courtId",
        Court::class,
        authenticationHeader(requestContext),
      )

    return (
      if (result.errors.isEmpty()) {
        Response(data = result.data!!)
      } else {
        Response.error(UpstreamApi.COURT_REGISTER, result.errors, null)
      }
    )
  }

  private fun authenticationHeader(requestContext: RequestContext? = null): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("COURT_REGISTER", requestContext)
    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }

  internal fun useRestApiClient() = features.isEnabled(FeatureFlagConfig.RESTAPICLIENT_FOR_COURT_REGISTER_GATEWAYY)
}

@Configuration
class RestClientConfigCourtRegister {
  @Bean("courtRegisterRestClient")
  fun courtRegisterRestClient(
    @Value("\${services.court-register.base-url}") baseUrl: String,
  ): RestApiClient = RestApiClient(UpstreamApi.COURT_REGISTER.name, baseUrl)
}
