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

class FiltersExtractionFilterTest {
  private var authorisationConfig: AuthorisationConfig = AuthorisationConfig()
  private var filtersExtractionFilter: FiltersExtractionFilter = FiltersExtractionFilter(authorisationConfig)

  @Test
  fun `calls the onward chain`() {
    // Arrange
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.getAttribute("clientName")).thenReturn("consumer-name")

    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)

    authorisationConfig.consumers = mapOf("consumer-name" to ConsumerConfig(include = null, filters = ConsumerFilters(prisons = null)))

    // Act
    filtersExtractionFilter.doFilter(mockRequest, mockResponse, mockChain)

    // Assert
    verify(mockChain, times(1)).doFilter(mockRequest, mockResponse)
  }

  @Test
  fun `can get filters attribute from the test`() {
    // Arrange
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.getAttribute("clientName")).thenReturn("consumer-name")

    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)

    val expectedFilters = ConsumerFilters(prisons = listOf("filter-1", "filter-2"))
    authorisationConfig.consumers = mapOf("consumer-name" to ConsumerConfig(include = null, filters = expectedFilters))

    // Act
    filtersExtractionFilter.doFilter(mockRequest, mockResponse, mockChain)

    // Assert
    verify(mockRequest, times(1)).setAttribute("filters", expectedFilters)
  }

  @Test
  fun `tolerates when no consumer config values matched with clientName`() {
    // Arrange
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.getAttribute("clientName")).thenReturn("invalid-consumer-name")

    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)

    val expectedFilters = ConsumerFilters(prisons = listOf("filter-1", "filter-2"))
    authorisationConfig.consumers = mapOf("consumer-name" to ConsumerConfig(include = null, filters = expectedFilters))

    // Act
    filtersExtractionFilter.doFilter(mockRequest, mockResponse, mockChain)

    // Assert
    verify(mockRequest, times(0)).setAttribute("filters", expectedFilters)
  }
}
