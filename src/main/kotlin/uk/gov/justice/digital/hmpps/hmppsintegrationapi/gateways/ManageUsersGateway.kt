package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.CacheConfig.Companion.HMPPS_AUTH_USERS
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.manageUsers.PaginatedUsers

@Component
class ManageUsersGateway(
  @Value("\${services.manage-users.base-url}") baseUrl: String,
  private val hmppsAuthGateway: HmppsAuthGateway,
) : UpstreamGateway {
  override fun metaData() =
    GatewayMetadata(
      summary = "Manage Users",
      developerPortalId = "DPS017",
      developerPortalUrl = "https://developer-portal.hmpps.service.justice.gov.uk/components/hmpps-manage-users-api",
      apiDocUrl = "https://manage-users-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html#/",
      apiSpecUrl = "https://manage-users-api-dev.hmpps.service.justice.gov.uk/v3/api-docs",
      gitHubRepoUrl = "https://github.com/ministryofjustice/hmpps-manage-users-api",
    )

  private val webClient = WebClientWrapper(baseUrl)

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken(UpstreamApi.MANAGE_USERS.name)
    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }

  @Cacheable(HMPPS_AUTH_USERS, keyGenerator = "hmppsAuthUsersCacheKeyGenerator")
  fun findUser(
    username: String,
    authSources: List<String>,
  ): Response<PaginatedUsers?> {
    val uri =
      UriComponentsBuilder
        .fromUriString("/users/search")
        .queryParam("username", username)
        .queryParam("authSources", authSources)
        .toUriString()

    val result =
      webClient.request<PaginatedUsers>(
        HttpMethod.GET,
        uri,
        authenticationHeader(),
        UpstreamApi.MANAGE_USERS,
        badRequestAsError = true,
      )

    return when (result) {
      is WebClientWrapper.WebClientWrapperResponse.Success -> {
        Response(
          data = result.data,
        )
      }

      is WebClientWrapper.WebClientWrapperResponse.Error -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }
}
