package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import io.sentry.Sentry
import org.apache.tomcat.util.json.JSONParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.ENABLE_WRAP_AUTH
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.HmppsAuthFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Credentials
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Base64

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

  private var existingAccessToken: String? = null

  private fun checkTokenValid(token: String): Boolean =
    try {
      val encodedPayload = token.split(".")[1]
      val decodedToken = String(Base64.getDecoder().decode(encodedPayload), StandardCharsets.UTF_8)
      val now = Instant.now().epochSecond
      val expiration = JSONParser(decodedToken).parseObject()["exp"].toString().toLong()
      (now < (expiration - 5))
    } catch (e: Exception) {
      Sentry.captureException(e)
      false
    }

  fun reset() {
    existingAccessToken = null
  }

  override fun getClientToken(service: String): String {
    existingAccessToken?.let {
      if (checkTokenValid(it)) {
        telemetryService.trackEvent("AuthTokenCache")
        return it
      }
    }

    telemetryService.trackEvent("AuthTokenRequest")
    val credentials = Credentials(username, password)
    val uri = "/auth/oauth/token?grant_type=client_credentials"

    val enableWrap = featureFlag.getConfigFlagValue(ENABLE_WRAP_AUTH)

    return try {
      var response: String?
      if (enableWrap!!) {
        val result =
          webClientWrapper.request<String>(
            HttpMethod.POST,
            uri,
            mapOf("Authorization" to credentials.toBasicAuth()),
            UpstreamApi.AUTH,
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
      this.existingAccessToken = accessToken
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
