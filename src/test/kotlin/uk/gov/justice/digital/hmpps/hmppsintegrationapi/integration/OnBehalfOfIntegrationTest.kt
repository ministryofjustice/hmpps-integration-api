package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.AuthorisationFilter
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.onbehalfof.createUnassignedJwy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import kotlin.test.Test

@AutoConfigureMockMvc
class OnBehalfOfIntegrationTest : IntegrationTestBase() {
  val mockRequest = mock(HttpServletRequest::class.java)
  val mockResponse = mock(HttpServletResponse::class.java)
  val mockChain = mock(FilterChain::class.java)
  val mockTelemetryService = mock(TelemetryService::class.java)

  lateinit var authorisationFilter: AuthorisationFilter

  @BeforeEach
  fun setup() {
    whenever(mockRequest.requestURI).thenReturn("/v1/persons/$crn")
    authorisationFilter =
      AuthorisationFilter(
        authorisationService,
        mockTelemetryService,
      )
    whenever(mockRequest.getHeader("cert-serial-number")).thenReturn(certSerialNumber)
    whenever(mockRequest.getHeader("X-On-Behalf-Of")).thenReturn(createUnassignedJwy())
  }

  @Test
  fun `if oboConfig is empty, do not call any obo service`() {
    whenever(mockRequest.getHeader("subject-distinguished-name")).thenReturn("C=GB,ST=London,L=London,O=Home Office,CN=obo-empty")
    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)
    verify(authorisationService, times(1)).oboService("obo-empty")
  }

  @Test
  fun `if oboConfig is unsigned, call the unsigned obo service`() {
    whenever(mockRequest.getHeader("subject-distinguished-name")).thenReturn("C=GB,ST=London,L=London,O=Home Office,CN=obo-unsigned")
    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)
    verify(authorisationService, times(1)).oboService("obo-unsigned")
  }

  @Test
  fun `if oboConfig is entra, do not call any obo service`() {
    whenever(mockRequest.getHeader("subject-distinguished-name")).thenReturn("C=GB,ST=London,L=London,O=Home Office,CN=obo-entra")
    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)
    verify(authorisationService, times(1)).oboService("obo-entra")
  }

  @Test
  fun `if oboConfig is invalid, do not call any obo service`() {
    whenever(mockRequest.getHeader("subject-distinguished-name")).thenReturn("C=GB,ST=London,L=London,O=Home Office,CN=obo-invalid")
    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)
    verify(authorisationService, times(1)).oboService("obo-invalid")
  }
}
