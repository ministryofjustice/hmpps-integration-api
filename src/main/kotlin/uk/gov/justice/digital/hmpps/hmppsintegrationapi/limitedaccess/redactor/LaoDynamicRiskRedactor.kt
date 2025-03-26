package uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor.Redactor.Companion.REDACTED
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DynamicRisk

object LaoDynamicRiskRedactor : Redactor<DynamicRisk> {
  override val type = DynamicRisk::class

  override fun redact(toRedact: Any): DynamicRisk =
    when (toRedact) {
      is DynamicRisk -> toRedact.copy(notes = REDACTED)
      else -> throw IllegalArgumentException("${this::class.simpleName} unable to redact ${toRedact::class.simpleName}")
    }
}
