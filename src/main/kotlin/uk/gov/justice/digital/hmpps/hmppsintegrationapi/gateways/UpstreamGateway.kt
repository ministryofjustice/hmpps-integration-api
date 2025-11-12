package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

data class GatewayMetadata(
  val summary: String,
  val apiDocUrl: String? = null,
  val apiSpecUrl: String? = null,
  val gitHubRepoUrl: String? = null,
  val developerPortalId: String? = null,
  val developerPortalUrl: String? = null,
  val slackChannel: String? = null,
)

interface UpstreamGateway {
  fun metaData(): GatewayMetadata
}
