package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
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
) {
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

  fun getResidentialSummary(prisonId: String): Response<LIPResidentialSummary?> {
    val result =
      webClient.request<LIPResidentialSummary>(
        HttpMethod.GET,
        "/locations/residential-summary/$prisonId",
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

  fun getResidentialHierarchy(prisonId: String): Response<List<LIPResidentialHierarchyItem>?> {
    val result =
      webClient.requestList<LIPResidentialHierarchyItem>(
        HttpMethod.GET,
        "/locations/prison/$prisonId/residential-hierarchy",
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
