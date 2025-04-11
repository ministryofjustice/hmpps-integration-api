package uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.StatusInformation

class LaoStatusInformationRedactorTest {
  @Test
  fun `can redact status information`() {
    val toRedact = givenRedactable()
    val result = LaoStatusInformationRedactor.redact(toRedact)
    assertThat(result.notes).isEqualTo(Redactor.REDACTED)
  }

  private fun givenRedactable() = StatusInformation(notes = "Some text that needs to be redacted")
}
