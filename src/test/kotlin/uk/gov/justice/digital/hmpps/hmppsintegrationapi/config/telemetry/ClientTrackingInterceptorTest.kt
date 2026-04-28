package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.telemetry

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService

class ClientTrackingInterceptorTest {
  private val request: HttpServletRequest = mock(HttpServletRequest::class.java)
  private val response: HttpServletResponse = mock(HttpServletResponse::class.java)
  private val telemetryService = mock(TelemetryService::class.java)
  private val interceptor = ClientTrackingInterceptor(telemetryService)

  @Test
  fun `handles clientName attribute`() {
    whenever(request.getAttribute("clientName")).thenReturn("TEST_NAME")
    val result = interceptor.preHandle(request, response, "")
    assertTrue(result)
    verify(telemetryService, times(1)).setSpanAttribute("clientId", "TEST_NAME")
  }

  @Test
  fun `handles missing clientName attribute`() {
    whenever(request.getAttribute("clientName")).thenReturn(null)
    val result = interceptor.preHandle(request, response, "")
    assertTrue(result)
    verify(telemetryService, times(0)).setSpanAttribute("clientId", "TEST_NAME")
  }

  @Test
  fun `handles a certificate serial number header`() {
    whenever(request.getAttribute("certificateSerialNumber")).thenReturn("TEST_SERIAL")
    val result = interceptor.preHandle(request, response, "")
    assertTrue(result)
    verify(telemetryService, times(1)).setSpanAttribute("certSerialNumber", "TEST_SERIAL")
  }

  @Test
  fun `handles a NULL certificate serial number header`() {
    whenever(request.getHeader("certificateSerialNumber")).thenReturn(null)
    val result = interceptor.preHandle(request, response, "")
    assertTrue(result)
    verify(telemetryService, times(0)).setSpanAttribute("certSerialNumber", "TEST_SERIAL")
  }

  @Test
  fun `handles a on behalf of header`() {
    whenever(request.getHeader("X-On-Behalf-Of")).thenReturn("TEST_BEHALF_OF")
    val result = interceptor.preHandle(request, response, "")
    assertTrue(result)
    verify(telemetryService, times(1)).setSpanAttribute("certOnBehalfOff", "TEST_BEHALF_OF")
  }

  @Test
  fun `handles a NULL on behalf of header`() {
    whenever(request.getHeader("X-On-Behalf-Of")).thenReturn(null)
    val result = interceptor.preHandle(request, response, "")
    assertTrue(result)
    verify(telemetryService, times(0)).setSpanAttribute("certOnBehalfOff", "TEST_BEHALF_OF")
  }
}
