package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Court
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi

@Component
class CourtRegisterGateway(
  val courtRegisterRestClient: RestApiClient,
  var hmppsAuthGateway: HmppsAuthGateway,
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

  @Autowired
  fun getCourt(
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
}
