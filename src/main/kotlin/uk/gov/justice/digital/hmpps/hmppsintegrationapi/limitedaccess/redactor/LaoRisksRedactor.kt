package uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor.Redactor.Companion.REDACTED
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Risk
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskSummary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskToSelf
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Risks

object LaoRisksRedactor : Redactor<Risks> {
  override val type = Risks::class

  override fun redact(toRedact: Any): Risks =
    when (toRedact) {
      is Risks ->
        toRedact.copy(
          riskToSelf = toRedact.riskToSelf.redacted(),
          summary = toRedact.summary.redacted(),
        )

      else -> throw IllegalArgumentException("${this::class.simpleName} unable to redact ${toRedact::class.simpleName}")
    }

  fun RiskToSelf.redacted() =
    copy(
      suicide = suicide.redacted(),
      selfHarm = selfHarm.redacted(),
      custody = custody.redacted(),
      hostelSetting = hostelSetting.redacted(),
      vulnerability = vulnerability.redacted(),
    )

  fun Risk.redacted() = copy(previousConcernsText = if (previousConcernsText == null) null else REDACTED, currentConcernsText = if (currentConcernsText == null) null else REDACTED)

  fun RiskSummary.redacted() = RiskSummary(REDACTED, REDACTED, REDACTED, REDACTED, REDACTED, REDACTED, mapOf(REDACTED to REDACTED), mapOf(REDACTED to REDACTED))
}
