package uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor

import jakarta.servlet.http.HttpServletRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.GetCaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Licence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LicenceCondition
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonLicences
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.CaseAccess

class LaoLicenceConditionRedactorTest {
  private val request: HttpServletRequest = mock()
  private val accessForMock: GetCaseAccess = mock()

  @BeforeEach
  fun setUp() {
    whenever(request.getAttribute("encodedHmppsId")).thenReturn("12345")
    RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
    whenever(accessForMock.getAccessFor(any())).thenReturn(CaseAccess("crn", true, false))
  }

  @AfterEach
  fun clearRequestContext() {
    RequestContextHolder.resetRequestAttributes()
  }

  @Test
  fun `can redact person licence conditions`() {
    val licences = givenRedactable()
    val result = LaoPersonLicencesRedactor(accessForMock).redact(licences)
    assertThat(result.licences.flatMap { it.conditions.map { it.condition } }.single()).isEqualTo(Redactor.REDACTED)
  }

  private fun givenRedactable() = PersonLicences("L123456", licences = listOf(Licence("LIC1", conditions = listOf(LicenceCondition(condition = "Some secret condition that needs redacting")))))
}
