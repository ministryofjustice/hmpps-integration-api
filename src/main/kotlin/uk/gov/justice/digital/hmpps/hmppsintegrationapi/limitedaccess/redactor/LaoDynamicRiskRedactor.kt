package uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.AccessFor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor.Redactor.Companion.REDACTED
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DynamicRisk

class LaoDynamicRiskRedactor(
  loaChecker: AccessFor,
) : LaoBaseRedactor<DynamicRisk>(loaChecker) {
  override val type = DynamicRisk::class

  override fun redact(toRedact: Any): DynamicRisk {
    val response =
      toRedact as? DynamicRisk
        ?: throw IllegalArgumentException("Expected DataResponse, got ${toRedact::class.simpleName}")
    if (getLaoContext()!!.isLimitedAccess()) {
      return response.copy(notes = REDACTED)
    }
    return response
  }
}
