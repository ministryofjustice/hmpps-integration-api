package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.apache.tomcat.util.json.JSONParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.CacheConfig.Companion.GATEWAY_CACHE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_WEBCLIENT_WRAPPER_FOR_HMPPS_AUTH
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.HmppsAuthFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Credentials
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService

@Component
class HmppsAuthGateway(
  @Autowired val featureFlag: FeatureFlagConfig,
  @Value("\${services.hmpps-auth.base-url}") hmppsAuthUrl: String,
) : IAuthGateway,
  UpstreamGateway {
  override fun metaData() =
    GatewayMetadata(
      summary = "Our purpose is to identify staff and external users, and provide them with the access to digital prison and probation services they need to do their job, and no more.",
      developerPortalId = "DPS017",
      developerPortalUrl = "https://developer-portal.hmpps.service.justice.gov.uk/components/hmpps-authorization-api",
      gitHubRepoUrl = "https://github.com/ministryofjustice/hmpps-auth",
    )

  private val webClient: WebClient = WebClient.builder().baseUrl(hmppsAuthUrl).build()

  private val webClientWrapper = WebClientWrapper(hmppsAuthUrl)

  @Value("\${services.hmpps-auth.username}")
  private lateinit var username: String

  @Value("\${services.hmpps-auth.password}")
  private lateinit var password: String

  @Autowired
  private lateinit var featureFlagConfig: FeatureFlagConfig

  @Autowired
  private lateinit var telemetryService: TelemetryService

  @Cacheable(GATEWAY_CACHE, keyGenerator = "gatewayKeyGenerator", condition = "@gatewayCacheEnabled")
  override fun getClientToken(
    service: String,
    requestContext: RequestContext?,
  ): String {
    telemetryService.trackEvent("AuthTokenRequest")
    val credentials = Credentials(username, password)
    val uri = "/auth/oauth/token?grant_type=client_credentials${requestContext?.oboUserName?.let {"&username=${requestContext.oboUserName}"} ?: ""}"
    return try {
      var response: String?
      if (featureFlag.isEnabled(USE_WEBCLIENT_WRAPPER_FOR_HMPPS_AUTH)) {
        val result =
          webClientWrapper.request<String>(
            HttpMethod.POST,
            uri,
            mapOf("Authorization" to credentials.toBasicAuth()),
            UpstreamApi.HMPPS_AUTH,
          )

        when (result) {
          is WebClientWrapperResponse.Success -> {
            response = result.data
          }

          is WebClientWrapperResponse.Error -> {
            when (result.errors.map { it.type }.firstOrNull()) {
              UpstreamApiError.Type.FORBIDDEN -> throw HmppsAuthFailedException("Invalid credentials used for $service.")
              UpstreamApiError.Type.ENTITY_NOT_FOUND -> throw HmppsAuthFailedException("$uri is unavailable for $service.")
              else -> throw HmppsAuthFailedException("Connection to $uri failed for $service.")
            }
          }
        }
      } else {
        response =
          webClient
            .post()
            .uri(uri)
            .header("Authorization", credentials.toBasicAuth())
            .retrieve()
            .bodyToMono(String::class.java)
            .block()
      }

      val accessToken = JSONParser(response).parseObject()["access_token"].toString()
      accessToken
    } catch (exception: WebClientRequestException) {
      throw HmppsAuthFailedException("Connection to ${exception.uri.authority} failed for $service.")
    } catch (exception: WebClientResponseException.ServiceUnavailable) {
      throw HmppsAuthFailedException("${exception.request?.uri?.authority} is unavailable for $service.")
    } catch (exception: WebClientResponseException.Unauthorized) {
      throw HmppsAuthFailedException("Invalid credentials used for $service.")
    }
  }
}
