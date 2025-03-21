package uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Licence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LicenceCondition
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonLicences

class LaoLicenceConditionRedactorTest {
  @Test
  fun `can redact person licence conditions`() {
    val licences = givenRedactable()
    val result = LaoPersonLicencesRedactor.redact(licences)
    assertThat(result.licences.flatMap { it.conditions.map { it.condition } }.single()).isEqualTo(Redactor.REDACTED)
  }

  private fun givenRedactable() = PersonLicences("L123456", licences = listOf(Licence("LIC1", conditions = listOf(LicenceCondition(condition = "Some secret condition that needs redacting")))))
}
