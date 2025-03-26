package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DynamicRisk
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.MappaDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Offence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Sentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.StatusInformation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.CaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.NDeliusSupervisions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.UserAccess

@Component
class NDeliusGateway(
  @Value("\${services.ndelius.base-url}") baseUrl: String,
) {
  private val webClient = WebClientWrapper(baseUrl)

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  fun getOffencesForPerson(id: String): Response<List<Offence>> {
    val result =
      webClient.request<NDeliusSupervisions>(
        HttpMethod.GET,
        "/case/$id/supervisions",
        authenticationHeader(),
        UpstreamApi.NDELIUS,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data.supervisions.flatMap { it.toOffences() })
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = emptyList(),
          errors = result.errors,
        )
      }
    }
  }

  fun getSentencesForPerson(id: String): Response<List<Sentence>> {
    val result =
      webClient.request<NDeliusSupervisions>(
        HttpMethod.GET,
        "/case/$id/supervisions",
        authenticationHeader(),
        UpstreamApi.NDELIUS,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data.supervisions.map { it.toSentence() })
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = emptyList(),
          errors = result.errors,
        )
      }
    }
  }

  fun getDynamicRisksForPerson(id: String): Response<List<DynamicRisk>> {
    val result =
      webClient.request<NDeliusSupervisions>(
        HttpMethod.GET,
        "/case/$id/supervisions",
        authenticationHeader(),
        UpstreamApi.NDELIUS,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data.dynamicRisks.map { it.toDynamicRisk() })
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = emptyList(),
          errors = result.errors,
        )
      }
    }
  }

  fun getStatusInformationForPerson(id: String): Response<List<StatusInformation>> {
    val result =
      webClient.request<NDeliusSupervisions>(
        HttpMethod.GET,
        "/case/$id/supervisions",
        authenticationHeader(),
        UpstreamApi.NDELIUS,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data.personStatus.map { it.toStatusInformation() })
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = emptyList(),
          errors = result.errors,
        )
      }
    }
  }

  fun getMappaDetailForPerson(id: String): Response<MappaDetail?> {
    val result =
      webClient.request<NDeliusSupervisions>(
        HttpMethod.GET,
        "/case/$id/supervisions",
        authenticationHeader(),
        UpstreamApi.NDELIUS,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data.mappaDetail?.toMappaDetail())
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }

  fun getCommunityOffenderManagerForPerson(id: String): Response<CommunityOffenderManager> {
    val result =
      webClient.request<NDeliusSupervisions>(
        HttpMethod.GET,
        "/case/$id/supervisions",
        authenticationHeader(),
        UpstreamApi.NDELIUS,
      )
    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data.communityManager.toCommunityOffenderManager())
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = CommunityOffenderManager(),
          errors = result.errors,
        )
      }
    }
  }

  fun getCaseAccess(crn: String): Response<CaseAccess?> {
    val result =
      webClient.request<UserAccess>(
        HttpMethod.POST,
        "/probation-cases/access",
        authenticationHeader(),
        UpstreamApi.NDELIUS,
        mapOf("crns" to listOf(crn)),
      )
    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(result.data.access.firstOrNull { it.crn == crn })
      }

      is WebClientWrapperResponse.Error -> {
        Response(null, result.errors)
      }
    }
  }

  private fun authenticationHeader(): Map<String, String> {
    val token = hmppsAuthGateway.getClientToken("nDelius")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }
}
