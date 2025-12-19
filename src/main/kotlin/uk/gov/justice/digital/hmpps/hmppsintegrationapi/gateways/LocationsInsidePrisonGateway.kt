package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison.LIPLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison.LIPResidentialHierarchyItem
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison.LIPResidentialSummary

@Component
class LocationsInsidePrisonGateway(
  @Value("\${services.locations-inside-prison.base-url}") baseUrl: String,
) : UpstreamGateway {
  override fun metaData() =
    GatewayMetadata(
      summary = "Residential Locations is used to manage the residential and internal locations of the prison estate.",
      developerPortalId = "DPS038",
      developerPortalUrl = "https://developer-portal.hmpps.service.justice.gov.uk/components/hmpps-locations-inside-prison-api",
      apiDocUrl = "https://locations-inside-prison-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html#/",
      apiSpecUrl = "https://locations-inside-prison-api-dev.hmpps.service.justice.gov.uk/v3/api-docs",
      gitHubRepoUrl = "https://github.com/ministryofjustice/hmpps-locations-inside-prison-api",
    )

  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getLocationByKey(key: String): Response<LIPLocation?> {
    val result =
      webClient.request<LIPLocation>(
        HttpMethod.GET,
        "locations/key/$key",
        authenticationHeader(),
        UpstreamApi.LOCATIONS_INSIDE_PRISON,
        badRequestAsError = true,
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

  fun getResidentialSummary(
    prisonId: String,
    parentPathHierarchy: String? = null,
  ): Response<LIPResidentialSummary?> {
    val result =
      webClient.request<LIPResidentialSummary>(
        HttpMethod.GET,
        "/locations/residential-summary/$prisonId" + if (parentPathHierarchy == null) "" else "?parentPathHierarchy=$parentPathHierarchy",
        authenticationHeader(),
        UpstreamApi.LOCATIONS_INSIDE_PRISON,
        badRequestAsError = true,
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

  fun getResidentialHierarchy(
    prisonId: String,
    includeInactive: Boolean = false,
  ): Response<List<LIPResidentialHierarchyItem>?> {
    val result =
      webClient.requestList<LIPResidentialHierarchyItem>(
        HttpMethod.GET,
        "/locations/prison/$prisonId/residential-hierarchy" + if (includeInactive) "?includeInactive=true" else "",
        authenticationHeader(),
        UpstreamApi.LOCATIONS_INSIDE_PRISON,
        badRequestAsError = true,
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
    val token = hmppsAuthGateway.getClientToken("LOCATIONS-INSIDE-PRISON")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
