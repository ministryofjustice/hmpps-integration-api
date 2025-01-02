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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuthoriseConsumerService

class AuthorisationFilterTest {
  private var authorisationConfig: AuthorisationConfig = AuthorisationConfig()
  private var authorisationFilter: AuthorisationFilter = AuthorisationFilter(authorisationConfig, AuthoriseConsumerService())
  private var examplePath: String = "/v1/persons"
  private var exampleConsumer: String = "consumer-name"

  @Test
  fun `calls the onward chain`() {
    // Arrange
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.requestURI).thenReturn(examplePath)
    whenever(mockRequest.getAttribute("clientName")).thenReturn(exampleConsumer)
    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)

    authorisationConfig.consumers = mapOf(exampleConsumer to ConsumerConfig(include = listOf(examplePath), filters = ConsumerFilters(emptyMap())))

    // Act
    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)

    // Assert
    verify(mockChain, times(1)).doFilter(mockRequest, mockResponse)
  }

  @Test
  fun `generates error when consumer is unauthorised for requested path`() {
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.requestURI).thenReturn(examplePath)
    whenever(mockRequest.getAttribute("clientName")).thenReturn(exampleConsumer)

    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)

    val mockService = mock(AuthoriseConsumerService::class.java)
    whenever(mockService.execute(exampleConsumer, authorisationConfig.consumers, examplePath))
      .thenReturn(false)

    val authorisationFilter = AuthorisationFilter(authorisationConfig, mockService)

    // Act
    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)

    // Assert
    verify(mockResponse, times(1)).sendError(403, "Unable to authorise /v1/persons for consumer-name")
  }

  @Test
  fun `generates error when subject distinguished name is null in the request`() {
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.requestURI).thenReturn(examplePath)
    whenever(mockRequest.getAttribute("clientName")).thenReturn(null)
    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)

    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)

    verify(mockResponse, times(1)).sendError(403, "No subject-distinguished-name header provided for authorisation")
  }
}
