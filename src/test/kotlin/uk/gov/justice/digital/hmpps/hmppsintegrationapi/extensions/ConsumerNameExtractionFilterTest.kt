package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class ConsumerNameExtractionFilterTest {
  private var consumerNameExtractionFilter: ConsumerNameExtractionFilter = ConsumerNameExtractionFilter()
  val testSerialNumber = "8472494320151578633330348943480876283449388195"
  val testSerialHexFormat = "01:7B:EB:77:06:DB:11:F5:2E:B6:F7:37:7B:A9:E0:E4:84:C5:2C:A3"

  @Test
  fun `calls the onward chain`() {
    // Arrange
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.getAttribute("clientName")).thenReturn("consumer-name")

    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)

    // Act
    consumerNameExtractionFilter.doFilter(mockRequest, mockResponse, mockChain)

    // Assert
    verify(mockChain, times(1)).doFilter(mockRequest, mockResponse)
  }

  @Test
  fun `can get subject distinguished name from request and set as consumer name `() {
    // Arrange
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.getHeader("subject-distinguished-name")).thenReturn(",CN=consumer-name")
    whenever(mockRequest.getHeader("cert-serial-number")).thenReturn(testSerialNumber)

    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)
    // Act
    consumerNameExtractionFilter.doFilter(mockRequest, mockResponse, mockChain)

    // Assert
    verify(mockRequest, times(1)).setAttribute("clientName", "consumer-name")
    verify(mockRequest, times(1)).setAttribute("certificateSerialNumber", testSerialHexFormat)
  }

  @Test
  fun `does not set a clientName from request if it does not match the regex `() {
    // Arrange
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.getHeader("subject-distinguished-name")).thenReturn("CN=consumer-name")

    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)
    // Act
    consumerNameExtractionFilter.doFilter(mockRequest, mockResponse, mockChain)

    // Assert
    verify(mockRequest, times(1)).setAttribute("clientName", null)
  }
}
