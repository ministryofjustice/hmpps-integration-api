package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerBaseLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi

@Component
class PrisonerBaseLocationGateway(
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @Value("\${services.prisoner-base-location.base-url}") baseUrl: String,
) : UpstreamGateway {
  override fun metaData() =
    GatewayMetadata(
      summary = "Prisoner Locations is used to manage the residential and internal locations of the prison estate.",
      developerPortalId = "DPS037",
      developerPortalUrl = "https://developer-portal.hmpps.service.justice.gov.uk/components/hmpps-prisoner-base-location-api",
      apiDocUrl = "https://prisoner-base-location-api.hmpps.service.justice.gov.uk/swagger-ui/index.html",
      apiSpecUrl = "https://prisoner-base-location-api.hmpps.service.justice.gov.uk/v3/api-docs",
      gitHubRepoUrl = "https://github.com/ministryofjustice/hmpps-prisoner-base-location-api",
    )

  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getPrisonerBaseLocation(nomisNumber: String): Response<PrisonerBaseLocation?> {
    val result =
      webClient.request<PrisonerBaseLocation>(
        HttpMethod.GET,
        "v1/persons/$nomisNumber/prisoner-base-location",
        authenticationHeader(),
        UpstreamApi.PRISONER_BASE_LOCATION,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data)
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("Prisoner Base Location")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
