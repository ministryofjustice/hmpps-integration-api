package uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonLicences

object LaoPersonLicencesRedactor : Redactor<PersonLicences> {
  override val type = PersonLicences::class

  override fun redact(toRedact: Any): PersonLicences =
    when (toRedact) {
      is PersonLicences -> toRedact.copy(licences = toRedact.licences.map { it.copy(conditions = it.conditions.map { it.copy(condition = Redactor.REDACTED) }) })
      else -> throw IllegalArgumentException("${this::class.simpleName} unable to redact ${toRedact::class.simpleName}")
    }
}
