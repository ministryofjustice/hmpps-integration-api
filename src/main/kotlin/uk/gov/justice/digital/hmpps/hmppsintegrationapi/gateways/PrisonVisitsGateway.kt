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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.PVPaginatedVisits
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.PVVisit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.VisitReferences

@Component
class PrisonVisitsGateway(
  @Value("\${services.manage-prison-visits.base-url}") baseUrl: String,
) {
  private val webClient = WebClientWrapper(baseUrl)
  private val mapper: ObjectMapper = ObjectMapper()

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getVisitByReference(visitReference: String): Response<PVVisit?> {
    val result =
      webClient.request<PVVisit?>(
        HttpMethod.GET,
        "/visits/$visitReference",
        authenticationHeader(),
        UpstreamApi.MANAGE_PRISON_VISITS,
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

  fun getVisits(
    prisonId: String,
    hmppsId: String?,
    fromDate: String?,
    toDate: String?,
    visitStatus: String,
    page: Int,
    size: Int,
  ): Response<PVPaginatedVisits?> {
    var queryString = "?prisonId=$prisonId&visitStatus=$visitStatus&page=$page&size=$size&"

    if (!hmppsId.isNullOrBlank()) {
      queryString += "prisonerId=$hmppsId&"
    }
    if (!fromDate.isNullOrBlank()) {
      queryString += "visitStartDate=$fromDate&"
    }
    if (!toDate.isNullOrBlank()) {
      queryString += "visitEndDate=$toDate"
    }

    val result =
      webClient.request<PVPaginatedVisits?>(
        HttpMethod.GET,
        "/visits/search$queryString",
        authenticationHeader(),
        UpstreamApi.MANAGE_PRISON_VISITS,
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

  fun getFutureVisits(prisonerId: String): Response<List<PVVisit>?> {
    val result =
      webClient.request<List<PVVisit>?>(
        HttpMethod.GET,
        "/visits/search/future/$prisonerId",
        authenticationHeader(),
        UpstreamApi.MANAGE_PRISON_VISITS,
        badRequestAsError = true,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(
          data = mapToVisits(result),
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

  fun getVisitReferencesByClientReference(clientReference: String): Response<VisitReferences?> {
    val result =
      webClient.request<List<String>>(
        HttpMethod.GET,
        "/visits/external-system/$clientReference",
        authenticationHeader(),
        UpstreamApi.MANAGE_PRISON_VISITS,
        badRequestAsError = true,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(
          data = VisitReferences(result.data),
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
    val token = hmppsAuthGateway.getClientToken("MANAGE-PRISON-VISITS")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }

  fun mapToVisits(result: WebClientWrapperResponse.Success<List<PVVisit>?>): List<PVVisit> {
    val mappedResult: List<PVVisit> = mapper.convertValue(result.data, object : TypeReference<List<PVVisit>>() {})
    return mappedResult
  }
}
