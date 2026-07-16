package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PlanCreationSchedules
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PlanReviewSchedules
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@Component
class SANGateway(
  @Value("\${services.san.base-url}") val baseUrl: String,
  val features: FeatureFlagConfig = FeatureFlagConfig(),
  val sanRestClient: RestApiClient? = null,
) : UpstreamGateway {
  override fun metaData() =
    GatewayMetadata(
      summary = "Support for Additional Needs",
      developerPortalId = "DPS124",
      developerPortalUrl = "https://developer-portal.hmpps.service.justice.gov.uk/components/hmpps-support-additional-needs-api",
      apiDocUrl = "https://support-for-additional-needs-api.hmpps.service.justice.gov.uk/swagger-ui/index.html",
      apiSpecUrl = "https://support-for-additional-needs-api.hmpps.service.justice.gov.uk/openapi/SupportAdditionalNeedsAPI.yml",
      gitHubRepoUrl = "https://github.com/ministryofjustice/hmpps-support-additional-needs-api",
      slackChannel = "#education-skills-work-employment-dev",
    )

  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getPlanCreationSchedules(prisonerNumber: String): Response<PlanCreationSchedules> {
    if (features.isEnabled(FeatureFlagConfig.RESTAPICLIENT_FOR_SAN_GATEWAY)) {
      return getPlanCreationSchedules2(prisonerNumber)
    }

    val result =
      webClient.request<PlanCreationSchedules>(
        HttpMethod.GET,
        "/profile/$prisonerNumber/plan-creation-schedule?includeAllHistory=true",
        authenticationHeader(),
        UpstreamApi.SAN,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        val planCreationSchedules = result.data
        Response(data = planCreationSchedules)
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = PlanCreationSchedules(listOf()),
          errors = result.errors,
        )
      }
    }
  }

  fun getPlanCreationSchedules2(prisonerNumber: String): Response<PlanCreationSchedules> {
    val result =
      sanRestClient!!.get(
        "/profile/$prisonerNumber/plan-creation-schedule?includeAllHistory=true",
        PlanCreationSchedules::class,
        authenticationHeader(),
      )

    return if (result.errors.isEmpty()) {
      Response(data = result.data!!)
    } else {
      Response(
        data = PlanCreationSchedules(listOf()),
        errors = wrapErrors(result.errors),
      )
    }
  }

  internal fun wrapErrors(errors: List<Exception>): List<UpstreamApiError> = errors.map { mapError(it) }

  internal fun mapError(error: Exception): UpstreamApiError =
    when (error) {
      is WebClientResponseException -> UpstreamApiError(UpstreamApi.SAN, mapStatus(error.statusCode), error.message)
      else -> UpstreamApiError(UpstreamApi.SAN, UpstreamApiError.Type.INTERNAL_SERVER_ERROR, error.message)
    }

  internal fun mapStatus(status: HttpStatusCode): UpstreamApiError.Type =
    when (status) {
      HttpStatus.NOT_FOUND -> UpstreamApiError.Type.ENTITY_NOT_FOUND
      HttpStatus.BAD_REQUEST -> UpstreamApiError.Type.BAD_REQUEST
      HttpStatus.FORBIDDEN -> UpstreamApiError.Type.FORBIDDEN
      else -> UpstreamApiError.Type.INTERNAL_SERVER_ERROR
    }

  fun getReviewSchedules(prisonerNumber: String): Response<PlanReviewSchedules> {
    if (features.isEnabled(FeatureFlagConfig.RESTAPICLIENT_FOR_SAN_GATEWAY)) {
      return getReviewSchedules2(prisonerNumber)
    }

    val result =
      webClient.request<PlanReviewSchedules>(
        HttpMethod.GET,
        "/profile/$prisonerNumber/reviews/review-schedules?includeAllHistory=true",
        authenticationHeader(),
        UpstreamApi.SAN,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        val planReviewSchedules = result.data
        Response(data = planReviewSchedules)
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = PlanReviewSchedules(listOf()),
          errors = result.errors,
        )
      }
    }
  }

  fun getReviewSchedules2(prisonerNumber: String): Response<PlanReviewSchedules> {
    val result =
      sanRestClient!!.get(
        "/profile/$prisonerNumber/reviews/review-schedules?includeAllHistory=true",
        PlanReviewSchedules::class,
        authenticationHeader(),
      )

    return if (result.errors.isEmpty()) {
      Response(data = result.data!!)
    } else {
      Response(
        data = PlanReviewSchedules(listOf()),
        errors = wrapErrors(result.errors),
      )
    }
  }

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("SAN")
    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}

@Configuration
class RestClientConfig {
  @Bean("sanRestClient")
  fun sanRestClient(
    @Value("\${services.san.base-url}") baseUrl: String,
  ): RestApiClient = RestApiClient(UpstreamApi.SAN.name, baseUrl)
}
