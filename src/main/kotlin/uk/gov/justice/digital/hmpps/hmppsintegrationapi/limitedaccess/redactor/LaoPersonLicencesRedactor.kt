package uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.AccessFor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonLicences

class LaoPersonLicencesRedactor(
  loaChecker: AccessFor,
) : LaoBaseRedactor<PersonLicences>(loaChecker) {
  override val type = PersonLicences::class

  override fun redact(toRedact: Any): PersonLicences {
    val response =
      toRedact as? PersonLicences
        ?: throw IllegalArgumentException("Expected DataResponse, got ${toRedact::class.simpleName}")

    if (getLaoContext()!!.isLimitedAccess()) {
      return response.copy(licences = response.licences.map { it.copy(conditions = it.conditions.map { it.copy(condition = Redactor.REDACTED) }) })
    }
    return response
  }
}
