package uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DynamicRisk

class LaoDynamicRiskRedactorTest {
  @Test
  fun `can redact dynamic risk`() {
    val toRedact = givenRedactable()
    val result = LaoDynamicRiskRedactor.redact(toRedact)
    assertThat(result.notes).isEqualTo(Redactor.REDACTED)
  }

  private fun givenRedactable() = DynamicRisk(notes = "Some text that needs to be redacted")
}
