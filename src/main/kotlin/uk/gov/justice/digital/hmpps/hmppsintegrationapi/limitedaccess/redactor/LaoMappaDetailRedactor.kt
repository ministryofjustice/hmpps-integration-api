package uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor.Redactor.Companion.REDACTED
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.MappaDetail

object LaoMappaDetailRedactor : Redactor<MappaDetail> {
  override val type = MappaDetail::class

  override fun redact(toRedact: Any): MappaDetail =
    when (toRedact) {
      is MappaDetail -> toRedact.copy(notes = REDACTED)
      else -> throw IllegalArgumentException("${this::class.simpleName} unable to redact ${toRedact::class.simpleName}")
    }
}
