package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.telemetry

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class ClientTrackingInterceptorTest {
  private val request: HttpServletRequest = mock(HttpServletRequest::class.java)
  private val response: HttpServletResponse = mock(HttpServletResponse::class.java)
  private val interceptor = ClientTrackingInterceptor()

  @Test
  fun `handles missing clientName attribute`() {
    whenever(request.getAttribute("clientName")).thenReturn(null)
    val result = interceptor.preHandle(request, response, "")
    assertTrue(result)
  }
}
