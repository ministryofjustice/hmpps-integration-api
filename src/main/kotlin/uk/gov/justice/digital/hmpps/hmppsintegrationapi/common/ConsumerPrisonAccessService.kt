package uk.gov.justice.digital.hmpps.hmppsintegrationapi.common

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class ConsumerPrisonAccessService {
  fun <T> checkPrisonerHasSupervisionStatus(
    prisoner: POSPrisoner?,
    filters: ConsumerFilters?,
  ): Response<T?> {
    val response = Response<T?>(data = null, errors = emptyList<UpstreamApiError>())

    if (filters?.supervisionStatus.isNullOrEmpty()) return response

    val containsPrison = filters?.supervisionStatus!!.contains("PRISON")
    val containsProbation = filters?.supervisionStatus!!.contains("PROBATION")
    if (containsPrison && containsProbation) return response

    val hasPrisonId = prisoner?.prisonId != null
    val inOutStatus = prisoner?.inOutStatus

    if (containsPrison && inOutStatus == "OUT" && hasPrisonId) {
      response.errors = listOf(
        UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found"),
      )
      return response
    }

    if (containsProbation && inOutStatus == "IN" && hasPrisonId) {
      response.errors = listOf(
        UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found"),
      )
      return response
    }

    return response

  }

  fun <T> checkConsumerHasPrisonAccess(
    prisonId: String?,
    filters: ConsumerFilters?,
    upstreamServiceType: UpstreamApi = UpstreamApi.PRISON_API,
  ): Response<T?> {
    val response = Response<T?>(data = null, errors = emptyList<UpstreamApiError>())
    if (filters != null && !filters.matchesPrison(prisonId)) {
      response.errors = listOf(UpstreamApiError(upstreamServiceType, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found"))
    }
    return response
  }
}
