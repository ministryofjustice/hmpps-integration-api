package uk.gov.justice.digital.hmpps.hmppsintegrationapi.common

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class ConsumerSupervisionStatusAccessService {
  fun checkConsumerHasSupervisionStatusAccess(
    prisoner: POSPrisoner?,
    filters: ConsumerFilters,
  ): Boolean {
    if (!filters.hasSupervisionStatusesFilter()) return true

    val containsPrison = filters.supervisionStatuses!!.contains("PRISON")
    val containsProbation = filters.supervisionStatuses.contains("PROBATION")

    if (containsPrison && containsProbation) return true

    val inOutStatus = prisoner?.inOutStatus

    if (containsProbation && inOutStatus == "OUT") {
      return true
    }

    if (containsPrison && inOutStatus == "IN") {
      return true
    }

    return false
  }
}
