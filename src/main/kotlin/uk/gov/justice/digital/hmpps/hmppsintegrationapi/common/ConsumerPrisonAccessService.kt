package uk.gov.justice.digital.hmpps.hmppsintegrationapi.common

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class ConsumerPrisonAccessService {
  fun <T> checkConsumerHasPrisonAccess(
    prisonId: String,
    filters: ConsumerFilters?,
  ): Response<T?> {
    val result = Response<T?>(data = null, errors = emptyList<UpstreamApiError>())
    if (filters != null && !filters.matchesPrison(prisonId)) {
      result.errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found"))
    }
    return result
  }
}
