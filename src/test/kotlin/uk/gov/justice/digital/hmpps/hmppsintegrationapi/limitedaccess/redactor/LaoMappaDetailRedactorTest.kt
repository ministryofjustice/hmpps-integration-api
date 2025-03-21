package uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.MappaDetail

class LaoMappaDetailRedactorTest {
  @Test
  fun `can redact mappa detail`() {
    val toRedact = givenRedactable()
    val result = LaoMappaDetailRedactor.redact(toRedact)
    assertThat(result.notes).isEqualTo(Redactor.REDACTED)
  }

  private fun givenRedactable() = MappaDetail(null, "Some text that needs to be redacted")
}
