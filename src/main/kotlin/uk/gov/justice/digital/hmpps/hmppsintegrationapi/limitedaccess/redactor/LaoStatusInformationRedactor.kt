package uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.AccessFor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor.Redactor.Companion.REDACTED
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.StatusInformation

class LaoStatusInformationRedactor(
  loaChecker: AccessFor,
) : LaoBaseRedactor<StatusInformation>(loaChecker) {
  override val type = StatusInformation::class

  override fun redact(toRedact: Any): StatusInformation {
    val response =
      toRedact as? StatusInformation
        ?: throw IllegalArgumentException("Expected DataResponse, got ${toRedact::class.simpleName}")

    if (getLaoContext()!!.isLimitedAccess()) {
      return response.copy(notes = REDACTED)
    }
    return response
  }
}
