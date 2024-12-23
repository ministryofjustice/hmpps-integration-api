package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

class AuthorisationFilterTest {
  private var authorisationConfig: AuthorisationConfig = AuthorisationConfig()
  private var authorisationFilter: AuthorisationFilter = AuthorisationFilter(authorisationConfig)

  @Test
  fun `calls the onward chain`() {
    // Arrange
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.requestURI).thenReturn("/v1/persons")
    whenever(mockRequest.getAttribute("clientName")).thenReturn("consumer-name")
    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)

    authorisationConfig.consumers = mapOf("consumer-name" to ConsumerConfig(include = listOf("/v1/persons"), filters = ConsumerFilters(emptyMap())))

    // Act
    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)

    // Assert
    verify(mockChain, times(1)).doFilter(mockRequest, mockResponse)
  }

  @Test
  fun `generates error when consumer is unauthorised for requested path`() {
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.requestURI).thenReturn("/v1/persons")
    whenever(mockRequest.getAttribute("clientName")).thenReturn("consumer-name")

    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)
    authorisationConfig.consumers = mapOf("consumer-name" to ConsumerConfig(include = null, filters = ConsumerFilters(emptyMap())))

    // Act
    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)

    // Assert
    verify(mockResponse, times(1)).sendError(403, "Unable to authorise /v1/persons for consumer-name")
  }
}
