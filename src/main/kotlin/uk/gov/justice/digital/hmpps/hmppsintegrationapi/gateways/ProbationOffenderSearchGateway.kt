package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AddressSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.PSAddressSearchResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.PSPaginatedOffendersResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.PersonSearchRequest

@Component
class ProbationOffenderSearchGateway(
  @Value("\${services.probation-offender-search.base-url}") baseUrl: String,
) : UpstreamGateway {
  override fun metaData() =
    GatewayMetadata(
      summary = "Provides search features for Delius elastic search",
      apiDocUrl = "https://probation-offender-search-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html",
      apiSpecUrl = "https://probation-offender-search-dev.hmpps.service.justice.gov.uk/v3/api-docs",
      gitHubRepoUrl = "https://github.com/ministryofjustice/probation-offender-search",
    )

  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun addressSearch(
    addressSearchRequest: AddressSearchRequest,
    maxResults: Int,
    requestContext: RequestContext?,
  ): Response<PSAddressSearchResponse?> {
    val result =
      webClient.request<PSAddressSearchResponse>(
        HttpMethod.POST,
        addressSearchRequest.uriString(maxResults),
        authenticationHeader(requestContext),
        UpstreamApi.PROBATION_OFFENDER_SEARCH,
        requestBody = addressSearchRequest.toMap(),
        badRequestAsError = true,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(
          data =
            result.data,
        )
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }

  fun personSearch(
    personSearchRequest: PersonSearchRequest,
    paginatedRequest: PaginatedRequest,
    requestContext: RequestContext?,
  ): Response<List<Person>> {
    val result =
      webClient.request<PSPaginatedOffendersResponse>(
        HttpMethod.POST,
        personSearchRequest.uriString(paginatedRequest.page, paginatedRequest.perPage),
        authenticationHeader(requestContext),
        UpstreamApi.PROBATION_OFFENDER_SEARCH,
        requestBody = personSearchRequest.toMap(),
        badRequestAsError = true,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(
          data =
            result.data.content
              .map { it.toPerson() }
              .sortedByDescending { it.dateOfBirth },
        )
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = emptyList(),
          errors = result.errors,
        )
      }
    }
  }

  private fun authenticationHeader(requestContext: RequestContext? = null): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("Probation Offender Search", requestContext)

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
