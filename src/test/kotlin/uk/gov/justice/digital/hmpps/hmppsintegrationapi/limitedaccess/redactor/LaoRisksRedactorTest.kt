package uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor.Redactor.Companion.REDACTED
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Risk
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskSummary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskToSelf
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Risks

class LaoRisksRedactorTest {
  @Test
  fun `can redact risks`() {
    val toRedact = risks("Some text that needs redacting")
    val result = LaoRisksRedactor.redact(toRedact)
    assertThat(result).isEqualTo(risks(REDACTED))
  }

  private fun risks(text: String) =
    Risks(
      riskToSelf =
        RiskToSelf(
          Risk(previousConcernsText = text, currentConcernsText = text),
          Risk(previousConcernsText = text, currentConcernsText = text),
          Risk(previousConcernsText = text, currentConcernsText = text),
          Risk(previousConcernsText = text, currentConcernsText = text),
          Risk(previousConcernsText = text, currentConcernsText = text),
        ),
      summary = RiskSummary(text, text, text, text, text, text, mapOf(text to text), mapOf(text to text)),
    )
}
