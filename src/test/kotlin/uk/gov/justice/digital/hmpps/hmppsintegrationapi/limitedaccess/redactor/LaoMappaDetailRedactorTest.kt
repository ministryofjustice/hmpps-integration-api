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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.MappaDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.CaseAccess

class LaoMappaDetailRedactorTest {
  private val request: HttpServletRequest = mock()
  private val accessForMock: GetCaseAccess = mock()

  @BeforeEach
  fun setUp() {
    whenever(request.getAttribute("hmppsId")).thenReturn("12345")
    RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
    whenever(accessForMock.getAccessFor(any())).thenReturn(CaseAccess("crn", true, false))
  }

  @AfterEach
  fun clearRequestContext() {
    RequestContextHolder.resetRequestAttributes()
  }

  @Test
  fun `can redact mappa detail`() {
    val toRedact = givenRedactable()
    val result = LaoMappaDetailRedactor(accessForMock).redact(toRedact)
    assertThat(result.notes).isEqualTo(Redactor.REDACTED)
  }

  private fun givenRedactable() = MappaDetail(null, "Some text that needs to be redacted")
}
