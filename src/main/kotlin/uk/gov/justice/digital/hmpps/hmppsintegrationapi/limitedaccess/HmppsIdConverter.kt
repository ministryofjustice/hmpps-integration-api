package uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway

interface CrnSupplier {
  fun getCrn(hmppsId: String): String?
}

@Service
class HmppsIdConverter(
  private val probationSearch: ProbationOffenderSearchGateway,
) : CrnSupplier {
  override fun getCrn(hmppsId: String): String? =
    if (hmppsId.matches(CRN_REGEX)) {
      hmppsId
    } else {
      probationSearch
        .getPerson(hmppsId)
        .data
        ?.identifiers
        ?.deliusCrn
    }

  companion object {
    val CRN_REGEX = "^[A-Z]\\d{6}$".toRegex()
  }
}
