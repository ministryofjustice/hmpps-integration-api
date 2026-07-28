package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.remandAndSentencing.RasSentencedCourtCases

@Component
class RemandAndSentencingGateway(
  val rasRestClient: RestApiClient,
  val hmppsAuthGateway: HmppsAuthGateway,
) : UpstreamGateway {
  override fun metaData() =
    GatewayMetadata(
      summary = "HMPPS Remand and Sentencing",
      developerPortalId = "DPS014",
      developerPortalUrl = "https://developer-portal.hmpps.service.justice.gov.uk/components/hmpps-remand-and-sentencing-api",
      apiDocUrl = "https://remand-and-sentencing-api-dev.hmpps.service.justice.gov.uk//swagger-ui/index.html",
      apiSpecUrl = "https://remand-and-sentencing-api-dev.hmpps.service.justice.gov.uk/v3/api-docs",
      gitHubRepoUrl = "https://github.com/ministryofjustice/hmpps-remand-and-sentencing-api",
      slackChannel = "#calculate_release_dates_public_channel",
    )

  fun getSentencedCourtCases(
    nomisNumber: String,
    requestContext: RequestContext? = null,
  ): Response<RasSentencedCourtCases> {
    val result =
      rasRestClient.get(
        "/person/$nomisNumber/sentenced-court-cases",
        RasSentencedCourtCases::class,
        authenticationHeader(requestContext),
      )

    return if (result.errors.isEmpty()) {
      Response(data = result.data!!)
    } else {
      Response.error(UpstreamApi.REMAND_AND_SENTENCING, result.errors, RasSentencedCourtCases())
    }
  }

  private fun authenticationHeader(requestContext: RequestContext? = null): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("REMAND_AND_SENTENCING", requestContext)
    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
