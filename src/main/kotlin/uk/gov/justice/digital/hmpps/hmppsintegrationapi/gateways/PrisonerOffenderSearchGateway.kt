package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.CacheConfig.Companion.GATEWAY_CACHE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPaginatedPrisoners
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner

@Component
class PrisonerOffenderSearchGateway(
  @Value("\${services.prisoner-offender-search.base-url}") baseUrl: String,
) : UpstreamGateway {
  override fun metaData() =
    GatewayMetadata(
      summary = "A service for searching for prisoners in NOMIS, augmented by data from other prison services",
      developerPortalId = "DPS072",
      developerPortalUrl = "https://developer-portal.hmpps.service.justice.gov.uk/components/hmpps-prisoner-search",
      apiDocUrl = "https://prisoner-search-dev.prison.service.justice.gov.uk/swagger-ui/index.html",
      apiSpecUrl = "https://prisoner-search-dev.prison.service.justice.gov.uk/v3/api-docs",
      gitHubRepoUrl = "https://github.com/ministryofjustice/hmpps-prisoner-search",
    )

  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getPersons(
    firstName: String?,
    lastName: String?,
    dateOfBirth: String?,
    searchWithinAliases: Boolean = false,
  ): Response<List<POSPrisoner>> {
    val maxNumberOfResults = 9999
    val requestBody =
      mapOf("firstName" to firstName, "lastName" to lastName, "includeAliases" to searchWithinAliases, "dateOfBirth" to dateOfBirth)
        .filterValues { it != null }

    val result =
      webClient.request<POSPaginatedPrisoners>(
        HttpMethod.POST,
        "/global-search?size=$maxNumberOfResults",
        authenticationHeader(),
        UpstreamApi.PRISONER_OFFENDER_SEARCH,
        requestBody,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(
          data = result.data.toPOSPrisoners(),
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

  fun getPrisonerDetails(
    firstName: String?,
    lastName: String?,
    dateOfBirth: String?,
    searchWithinAliases: Boolean = false,
    prisonIds: List<String?>?,
  ): Response<List<POSPrisoner>> {
    val maxNumberOfResults = 9999

    val requestBody =
      mapOf(
        "firstName" to firstName,
        "lastName" to lastName,
        "dateOfBirth" to dateOfBirth,
        "includeAliases" to searchWithinAliases,
        "prisonIds" to prisonIds,
        "pagination" to mapOf("page" to 0, "size" to maxNumberOfResults),
      ).filterValues { it != null }

    val result =
      webClient.request<POSPaginatedPrisoners>(
        HttpMethod.POST,
        "/prisoner-detail",
        authenticationHeader(),
        UpstreamApi.PRISONER_OFFENDER_SEARCH,
        requestBody,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(
          data = result.data.toPOSPrisoners(),
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

  @Cacheable(GATEWAY_CACHE, keyGenerator = "gatewayKeyGenerator", condition = "@gatewayCacheEnabled")
  fun getPrisonOffender(nomsNumber: String): Response<POSPrisoner?> {
    val result =
      webClient.request<POSPrisoner>(
        HttpMethod.GET,
        "/prisoner/$nomsNumber",
        authenticationHeader(),
        UpstreamApi.PRISONER_OFFENDER_SEARCH,
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

  fun attributeSearch(request: POSAttributeSearchRequest): Response<POSPaginatedPrisoners?> {
    val result =
      webClient.request<POSPaginatedPrisoners>(
        HttpMethod.POST,
        "/attribute-search",
        authenticationHeader(),
        UpstreamApi.PRISONER_OFFENDER_SEARCH,
        request.toMap(),
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(
          data = result.data,
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

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("Prisoner Offender Search")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
