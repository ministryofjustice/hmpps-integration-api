package uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor.Redactor.Companion.REDACTED
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.StatusInformation

object LaoStatusInformationRedactor : Redactor<StatusInformation> {
  override val type = StatusInformation::class

  override fun redact(toRedact: Any): StatusInformation =
    when (toRedact) {
      is StatusInformation -> toRedact.copy(notes = REDACTED)
      else -> throw IllegalArgumentException("${this::class.simpleName} unable to redact ${toRedact::class.simpleName}")
    }
}
