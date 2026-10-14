package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.tier.TierV3DetailsDto

@Component
class TierGateway(
  private val tierRestClient: RestApiClient,
  private val hmppsAuthGateway: HmppsAuthGateway,
) : UpstreamGateway {
  override fun metaData() =
    GatewayMetadata(
      summary = "HMPPS Tier calculator",
      developerPortalId = "HMPPS525",
      developerPortalUrl = "https://developer-portal.hmpps.service.justice.gov.uk/components/hmpps-tier",
      apiDocUrl = "https://hmpps-tier-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html",
      apiSpecUrl = "https://hmpps-tier-dev.hmpps.service.justice.gov.uk/v3/api-docs",
      gitHubRepoUrl = "https://github.com/ministryofjustice/hmpps-tier",
      slackChannel = "#probation-integration-team",
    )

  fun getTierDetails(
    crn: String,
    requestContext: RequestContext,
  ): Response<TierV3DetailsDto?> {
    val result =
      tierRestClient.get(
        "/v3/crn/$crn/tier/details",
        TierV3DetailsDto::class,
        authenticationHeader(requestContext),
      )

    return if (result.errors.isEmpty()) {
      Response(data = result.data!!)
    } else {
      Response.error(UpstreamApi.SAN, result.errors, null)
    }
  }

  private fun authenticationHeader(requestContext: RequestContext): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("TIER", requestContext)
    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
