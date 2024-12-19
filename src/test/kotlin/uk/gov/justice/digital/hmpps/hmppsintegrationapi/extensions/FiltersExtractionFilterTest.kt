@file:Suppress("ktlint:standard:no-wildcard-imports")

package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

class FiltersExtractionFilterTest {
  @Mock
  private var authorisationConfig: AuthorisationConfig = AuthorisationConfig()

  @InjectMocks
  private var filtersExtractionFilter: FiltersExtractionFilter = FiltersExtractionFilter(authorisationConfig)

  @Test
  fun `calls the onward chain`() {
    // Arrange
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.getAttribute("clientName")).thenReturn("consumer-name")

    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)

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

    val mockChain = mock(FilterChain::class.java)

    val expectedFilters = ConsumerFilters(mapOf("example-filter" to listOf("filter-1", "filter-2")))
    authorisationConfig.consumers = mapOf("consumer-name" to ConsumerConfig(include = null, filters = expectedFilters))

    // Act
    filtersExtractionFilter.doFilter(mockRequest, null, mockChain)

    // Assert
    verify(mockRequest, times(1)).setAttribute("filters", expectedFilters)
  }

  @Test
  fun `tolerates when no consumer config values matched with clientName`() {
    // Arrange
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.getAttribute("clientName")).thenReturn("invalid-consumer-name")

    val mockChain = mock(FilterChain::class.java)

    val expectedFilters = ConsumerFilters(mapOf("example-filter" to listOf("filter-1", "filter-2")))
    authorisationConfig.consumers = mapOf("consumer-name" to ConsumerConfig(include = null, filters = expectedFilters))

    // Act
    filtersExtractionFilter.doFilter(mockRequest, null, mockChain)

    // Assert
    verify(mockRequest, times(0)).setAttribute("filters", expectedFilters)
  }
}
