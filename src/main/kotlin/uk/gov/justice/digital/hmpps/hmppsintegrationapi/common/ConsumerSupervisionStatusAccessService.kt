package uk.gov.justice.digital.hmpps.hmppsintegrationapi.common

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

val IN_PRISON_STATUSES = listOf("ACTIVE_IN", "ACTIVE_OUT")
val RELEASED_STATUSES = listOf("INACTIVE_OUT")

@Service
class ConsumerSupervisionStatusAccessService {
  fun checkConsumerHasSupervisionStatusAccess(
    prisoner: POSPrisoner?,
    filters: ConsumerFilters,
  ): Boolean {
    if (!filters.hasSupervisionStatusesFilter()) return true

    val containsPrison = filters.supervisionStatuses!!.contains("PRISONS")
    val containsProbation = filters.supervisionStatuses.contains("PROBATION")

    if (containsPrison && containsProbation) return true

    val status = prisoner?.status

    if (containsPrison && IN_PRISON_STATUSES.contains(status)) {
      return true
    }
    if (containsProbation && RELEASED_STATUSES.contains(status)) {
      return true
    }
    return false
  }
}
