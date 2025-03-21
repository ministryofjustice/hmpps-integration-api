package uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.AccessFor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.CrnSupplier
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.LaoContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.LaoContextInterceptor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.CaseAccess

@ExtendWith(MockitoExtension::class)
class LaoContextInterceptorTest {
  @Mock
  lateinit var crnSupplier: CrnSupplier

  @Mock
  lateinit var laoChecker: AccessFor

  @Mock
  lateinit var request: HttpServletRequest

  @Mock
  lateinit var response: HttpServletResponse

  @InjectMocks
  lateinit var laoContextInterceptor: LaoContextInterceptor

  @ParameterizedTest
  @MethodSource("laoContextProvider")
  fun `populates LAO context correctly`(
    caseAccess: CaseAccess,
    context: LaoContext,
  ) {
    whenever(crnSupplier.getCrn(HMPPS_ID)).thenReturn(CRN)
    whenever(laoChecker.getAccessForCrn(CRN)).thenReturn(caseAccess)
    whenever(request.pathInfo).thenReturn("/v1/persons/$HMPPS_ID/status-information")

    laoContextInterceptor.preHandle(request, response, Object())

    verify(request).setAttribute(LaoContext::class.simpleName, context)
  }

  companion object {
    private const val HMPPS_ID = "N1234AB"
    private const val CRN = "N123456"

    @JvmStatic
    fun laoContextProvider() =
      listOf(
        Arguments.of(CaseAccess(CRN, true, true, "Exclusion Message", "Restriction Message"), LaoContext(CRN, true, true, "Exclusion Message", "Restriction Message")),
        Arguments.of(CaseAccess(CRN, true, false, "Exclusion Message", null), LaoContext(CRN, true, false, "Exclusion Message", null)),
        Arguments.of(CaseAccess(CRN, false, true, null, "Restriction Message"), LaoContext(CRN, false, true, null, "Restriction Message")),
        Arguments.of(CaseAccess(CRN, false, false, null, null), LaoContext(CRN, false, false, null, null)),
      )
  }
}
