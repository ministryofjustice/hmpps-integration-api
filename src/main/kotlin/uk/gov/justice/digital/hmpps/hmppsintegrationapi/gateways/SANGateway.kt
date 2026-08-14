package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PlanCreationSchedules
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PlanReviewSchedules
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi

@Component
class SANGateway(
  @Value("\${services.san.base-url}") val baseUrl: String,
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

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getPlanCreationSchedules(
    prisonerNumber: String,
    requestContext: RequestContext,
  ): Response<PlanCreationSchedules> {
    val result =
      sanRestClient!!.get(
        "/profile/$prisonerNumber/plan-creation-schedule?includeAllHistory=true",
        PlanCreationSchedules::class,
        authenticationHeader(requestContext),
      )

    return if (result.errors.isEmpty()) {
      Response(data = result.data!!)
    } else {
      Response.error(UpstreamApi.SAN, result.errors, PlanCreationSchedules(listOf()))
    }
  }

  fun getReviewSchedules(
    prisonerNumber: String,
    requestContext: RequestContext,
  ): Response<PlanReviewSchedules> {
    val result =
      sanRestClient!!.get(
        "/profile/$prisonerNumber/reviews/review-schedules?includeAllHistory=true",
        PlanReviewSchedules::class,
        authenticationHeader(requestContext),
      )

    return if (result.errors.isEmpty()) {
      Response(data = result.data!!)
    } else {
      Response.error(UpstreamApi.SAN, result.errors, PlanReviewSchedules(listOf()))
    }
  }

  private fun authenticationHeader(requestContext: RequestContext): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("SAN", requestContext)
    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
