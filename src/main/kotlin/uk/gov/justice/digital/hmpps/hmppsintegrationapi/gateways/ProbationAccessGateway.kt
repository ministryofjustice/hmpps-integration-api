package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.UserAccess

@Component
class ProbationAccessGateway(
  private val hmppsAuthGateway: HmppsAuthGateway,
  private val probationAccessRestClient: RestApiClient,
) : UpstreamGateway {
  override fun metaData() =
    GatewayMetadata(
      summary = "Probation Access Control",
      developerPortalId = "DPS017",
      developerPortalUrl = "https://developer-portal.hmpps.service.justice.gov.uk/components/probation-access-control",
      apiDocUrl = "https://probation-access-control-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html#",
      apiSpecUrl = "https://probation-access-control-dev.hmpps.service.justice.gov.uk/v3/api-docs",
      gitHubRepoUrl = "https://github.com/ministryofjustice/hmpps-probation-integration-services/tree/main/projects/probation-access-control",
    )

  fun getProbationAccess(
    username: String,
    crns: List<String>,
    requestContext: RequestContext? = null,
  ): Response<UserAccess?> {
    // Probation user access cannot take an empty list
    if (crns.isEmpty()) {
      return Response(UserAccess(emptyList()))
    }

    val result =
      probationAccessRestClient.post(
        "/user/$username/access",
        crns,
        UserAccess::class,
        authenticationHeader(requestContext),
      )

    return if (result.errors.isEmpty()) {
      Response(data = result.data)
    } else {
      Response.error(UpstreamApi.PROBATION_ACCESS, result.errors, null)
    }
  }

  private fun authenticationHeader(requestContext: RequestContext?): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken(UpstreamApi.PROBATION_ACCESS.name, requestContext)
    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
