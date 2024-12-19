package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

class FiltersExtractionFilterTest {
  @Test
  fun `calls the onward chain`() {
    // Arrange
    val mockRequest = mock(HttpServletRequest::class.java)
    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)

    val filter = FiltersExtractionFilter()

    // Act
    filter.doFilter(mockRequest, mockResponse, mockChain)

    // Assert
    verify(mockChain, times(1)).doFilter(mockRequest, mockResponse)
  }

  @Test
  fun `can get filters attribute from the test`() {
    // Arrange
    val mockRequest = mock(HttpServletRequest::class.java)
    val mockChain = mock(FilterChain::class.java)

    val filter = FiltersExtractionFilter()

    val authorisationConfig = AuthorisationConfig()
    val expectedFilters = ConsumerFilters(mapOf("example-filter" to listOf("filter-1", "filter-2")))
    authorisationConfig.consumers = mapOf("consumer-name" to ConsumerConfig(include = null, filters = expectedFilters))

    // Act
    filter.doFilter(mockRequest,  null, mockChain)

    // Assert
    verify(mockRequest, times(1)).setAttribute("filters", expectedFilters)
  }
}
