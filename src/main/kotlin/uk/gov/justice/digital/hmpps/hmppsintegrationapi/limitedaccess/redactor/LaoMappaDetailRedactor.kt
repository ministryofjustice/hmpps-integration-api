package uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.AccessFor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.MappaDetail

class LaoMappaDetailRedactor(
  loaChecker: AccessFor,
) : LaoBaseRedactor<MappaDetail>(loaChecker) {
  override val type = MappaDetail::class

  override fun redact(toRedact: Any): MappaDetail {
    val response =
      toRedact as? MappaDetail
        ?: throw IllegalArgumentException("Expected DataResponse, got ${toRedact::class.simpleName}")

    if (getLaoContext()!!.isLimitedAccess()) {
      return response.copy(notes = Redactor.REDACTED)
    }
    return response
  }
}
