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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.FutureVisit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.PaginatedVisit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.Visit

@Component
class PrisonVisitsGateway(
  @Value("\${services.manage-prison-visits.base-url}") baseUrl: String,
) {
  private val webClient = WebClientWrapper(baseUrl)
  private val mapper: ObjectMapper = ObjectMapper()

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getVisitByReference(visitReference: String): Response<Visit?> {
    val result =
      webClient.request<Visit?>(
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
    hmppsId: String,
    fromDate: String,
    toDate: String,
    visitStatus: String,
    page: Int,
    perPage: Int,
  ): Response<PaginatedVisit?> {
    val result =
      webClient.request<PaginatedVisit?>(
        HttpMethod.GET,
        "/visits/search?prisonerId=$hmppsId&prisonId=$prisonId&visitStartDate=$fromDate&visitEndDate=$toDate&visitStatus=$visitStatus&page=$page&size=$perPage",
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
  
  fun getFutureVisits(prisonerId: String): Response<List<FutureVisit>?> {
    val result =
      webClient.request<List<FutureVisit>?>(
        HttpMethod.GET,
        "/visits/search/future/$prisonerId",
        authenticationHeader(),
        UpstreamApi.MANAGE_PRISON_VISITS,
        badRequestAsError = true,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(
          data =
            mapToFutureVisits(result),
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

  fun mapToFutureVisits(result: WebClientWrapper.WebClientWrapperResponse.Success<List<FutureVisit>?>): List<FutureVisit> {
    val mappedResult: List<FutureVisit> = mapper.convertValue(result.data, object : TypeReference<List<FutureVisit>>() {})
    return mappedResult
  }
}
