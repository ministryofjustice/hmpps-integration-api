package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.EPF_ENDPOINT_INCLUDES_LAO
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper.WebClientWrapperResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DynamicRisk
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.MappaDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Offence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Sentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.StatusInformation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.CaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.NDeliusContactEvent
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.NDeliusContactEvents
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.NDeliusSupervisions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.UserAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationintegrationepf.EPFCaseDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationintegrationepf.LimitedAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.ContactDetailsWithAddress
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.Offender

@Component
class NDeliusGateway(
  @Value("\${services.ndelius.base-url}") baseUrl: String,
  @Autowired val featureFlag: FeatureFlagConfig,
) : UpstreamGateway {
  override fun metaData() =
    GatewayMetadata(
      summary = "Probation Integration API for NDelius access",
      developerPortalId = "national-delius",
      developerPortalUrl = "https://developer-portal.hmpps.service.justice.gov.uk/products/national-delius",
      apiDocUrl = "https://external-api-and-delius-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html",
      apiSpecUrl = "https://external-api-and-delius-dev.hmpps.service.justice.gov.uk/v3/api-docs",
      gitHubRepoUrl = "https://github.com/ministryofjustice/hmpps-probation-integration-services/tree/main/projects/external-api-and-delius",
      slackChannel = "#topic-hmpps-external-api-and-delius",
    )

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

  fun getCommunityOffenderManagerForPerson(crn: String): Response<CommunityOffenderManager?> {
    val result =
      webClient.request<NDeliusSupervisions?>(
        HttpMethod.GET,
        "/case/$crn/supervisions",
        authenticationHeader(),
        UpstreamApi.NDELIUS,
      )
    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data?.communityManager?.toCommunityOffenderManager())
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }

  fun getCaseAccess(crn: String): Response<CaseAccess?> {
    val result =
      webClient.requestWithRetry<UserAccess>(
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

  fun getEpfCaseDetailForPerson(
    id: String,
    eventNumber: Int,
  ): Response<CaseDetail?> {
    val result =
      webClient.request<EPFCaseDetail?>(
        HttpMethod.GET,
        "/case-details/$id/$eventNumber",
        authenticationHeader(),
        UpstreamApi.NDELIUS,
      )
    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(data = result.data?.toCaseDetail(includeLimitedAccess = featureFlag.isEnabled(EPF_ENDPOINT_INCLUDES_LAO)))
      }

      is WebClientWrapperResponse.Error -> {
        Response(
          data = null,
          errors = result.errors,
        )
      }
    }
  }

  fun getAccessLimitations(crn: String): Response<LimitedAccess?> {
    val result =
      webClient.request<LimitedAccess?>(
        HttpMethod.GET,
        "/case/$crn/access-limitations",
        authenticationHeader(),
        UpstreamApi.NDELIUS,
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
    val token = hmppsAuthGateway.getClientToken("nDelius")

    return mapOf(
      "Authorization" to "Bearer $token",
    )
  }

  fun getOffender(id: String? = null): Response<Offender?> {
    val queryField =
      if (isNomsNumber(id)) {
        "nomsNumber"
      } else {
        "crn"
      }

    val result =
      webClient.requestListWithRetry<Offender>(
        HttpMethod.POST,
        "/search/probation-cases",
        authenticationHeader(),
        UpstreamApi.NDELIUS,
        mapOf(queryField to id),
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        val persons = result.data
        val person = persons.firstOrNull()?.toPerson()

        Response(
          data = persons.firstOrNull(),
          errors =
            if (person == null) {
              listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.NDELIUS,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
              )
            } else {
              emptyList()
            },
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

  fun getPersons(
    firstName: String?,
    surname: String?,
    pncNumber: String?,
    dateOfBirth: String?,
    searchWithinAliases: Boolean = false,
  ): Response<List<Person>> {
    val requestBody =
      mapOf(
        "firstName" to firstName,
        "surname" to surname,
        "pncNumber" to pncNumber,
        "dateOfBirth" to dateOfBirth,
        "includeAliases" to searchWithinAliases,
      ).filterValues { it != null }

    val result =
      webClient.requestListWithRetry<Offender>(
        HttpMethod.POST,
        "/search/probation-cases",
        authenticationHeader(),
        UpstreamApi.NDELIUS,
        requestBody,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(
          data = result.data.map { it.toPerson() }.sortedByDescending { it.dateOfBirth },
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

  fun getAddressesForPerson(crn: String): Response<List<Address>> {
    val result =
      webClient.request<ContactDetailsWithAddress>(
        HttpMethod.GET,
        "/case/$crn/addresses",
        authenticationHeader(),
        UpstreamApi.NDELIUS,
      )

    return when (result) {
      is WebClientWrapperResponse.Success -> {
        return Response(
          result.data.contactDetails
            ?.addresses
            .orEmpty()
            .map { it.toAddress() },
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

  fun getContactEventsForPerson(
    crn: String,
    pageNo: Int,
    perPage: Int,
    mappaCategories: List<Number>?,
  ): Response<NDeliusContactEvents?> {
    val mappaCatQueryParam = "mappaCategories=${mappaCategories?.joinToString(",")}"
    val result =
      webClient.request<NDeliusContactEvents>(
        HttpMethod.GET,
        "/case/$crn/contacts?page=${pageNo - 1}&size=$perPage&$mappaCatQueryParam",
        authenticationHeader(),
        UpstreamApi.NDELIUS,
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

  fun getContactEventForPerson(
    crn: String,
    contactEventId: Long,
    mappaCategories: List<Number>?,
  ): Response<NDeliusContactEvent?> {
    val mappaCatQueryParam = "mappaCategories=${mappaCategories?.joinToString(",")}"

    val result =
      webClient.request<NDeliusContactEvent>(
        HttpMethod.GET,
        "/case/$crn/contacts/$contactEventId?$mappaCatQueryParam",
        authenticationHeader(),
        UpstreamApi.NDELIUS,
      )
    return when (result) {
      is WebClientWrapperResponse.Success -> {
        Response(result.data)
      }

      is WebClientWrapperResponse.Error -> {
        Response(null, result.errors)
      }
    }
  }

  private fun isNomsNumber(id: String?): Boolean = id?.matches(Regex("^[A-Z]\\d{4}[A-Z]{2}+$")) == true
}
