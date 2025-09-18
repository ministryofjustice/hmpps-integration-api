package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_ROLES_DSL
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.GlobalsConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.Role
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles

class FiltersExtractionFilterTest {
  private var authorisationConfig: AuthorisationConfig = AuthorisationConfig()
  private var globalsConfig: GlobalsConfig = GlobalsConfig()
  private var filtersExtractionFilter: FiltersExtractionFilter =
    FiltersExtractionFilter(
      authorisationConfig,
      globalsConfig,
      FeatureFlagConfig(mapOf(USE_ROLES_DSL to true)),
    )

  @BeforeEach
  fun setUp() {
    mockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
  }

  @AfterEach
  fun after() {
    unmockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
  }

  @Test
  fun `calls the onward chain`() {
    // Arrange
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.getAttribute("clientName")).thenReturn("consumer-name")

    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)

    authorisationConfig.consumers = mapOf("consumer-name" to ConsumerConfig(include = null, filters = ConsumerFilters(prisons = null), roles = listOf()))

    // Act
    filtersExtractionFilter.doFilter(mockRequest, mockResponse, mockChain)

    // Assert
    verify(mockChain, times(1)).doFilter(mockRequest, mockResponse)
  }

  @Test
  fun `can get prison filters attribute from the consumer config`() {
    // Arrange
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.getAttribute("clientName")).thenReturn("consumer-name")

    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)

    val expectedFilters = ConsumerFilters(prisons = listOf("filter-1", "filter-2"))
    val testRole = Role(include = null, filters = expectedFilters)
    authorisationConfig.consumers = mapOf("consumer-name" to ConsumerConfig(include = null, filters = ConsumerFilters(prisons = null), roles = listOf("test-role")))
    globalsConfig.roles = mapOf("test-role" to testRole)
    every { roles } returns mapOf("test-role" to testRole)

    // Act
    filtersExtractionFilter.doFilter(mockRequest, mockResponse, mockChain)

    // Assert
    verify(mockRequest, times(1)).setAttribute("filters", expectedFilters)
  }

  @Test
  fun `can get prison filters attribute from the role`() {
    // Arrange
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.getAttribute("clientName")).thenReturn("consumer-name")

    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)

    val expectedFilters = ConsumerFilters(prisons = listOf("filter-1", "filter-2"))
    val testRole = Role(include = null, filters = expectedFilters)
    authorisationConfig.consumers = mapOf("consumer-name" to ConsumerConfig(include = null, filters = ConsumerFilters(prisons = null), roles = listOf("test-role")))
    globalsConfig.roles = mapOf("test-role" to testRole)
    every { roles } returns mapOf("test-role" to testRole)

    // Act
    filtersExtractionFilter.doFilter(mockRequest, mockResponse, mockChain)

    // Assert
    verify(mockRequest, times(1)).setAttribute("filters", expectedFilters)
  }

  @Test
  fun `can get prison filters attribute from the role and the consumer`() {
    // Arrange
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.getAttribute("clientName")).thenReturn("consumer-name")

    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)

    val expectedFilters = ConsumerFilters(prisons = listOf("consumer-filter", "role-filter"))
    val testRole = Role(include = null, filters = ConsumerFilters(prisons = listOf("role-filter")))
    authorisationConfig.consumers = mapOf("consumer-name" to ConsumerConfig(include = null, filters = ConsumerFilters(prisons = listOf("consumer-filter")), roles = listOf("test-role")))
    globalsConfig.roles = mapOf("test-role" to testRole)
    every { roles } returns mapOf("test-role" to testRole)

    // Act
    filtersExtractionFilter.doFilter(mockRequest, mockResponse, mockChain)

    // Assert
    verify(mockRequest, times(1)).setAttribute("filters", expectedFilters)
  }

  @Test
  fun `can get case notes filters attribute from the consumer config`() {
    // Arrange
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.getAttribute("clientName")).thenReturn("consumer-name")

    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)

    val expectedFilters = ConsumerFilters(prisons = null, caseNotes = listOf("consumer-filter"))
    authorisationConfig.consumers = mapOf("consumer-name" to ConsumerConfig(include = null, filters = ConsumerFilters(prisons = null, caseNotes = listOf("consumer-filter")), roles = null))

    // Act
    filtersExtractionFilter.doFilter(mockRequest, mockResponse, mockChain)

    // Assert
    verify(mockRequest, times(1)).setAttribute("filters", expectedFilters)
  }

  @Test
  fun `can get case notes filters attribute from the role`() {
    // Arrange
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.getAttribute("clientName")).thenReturn("consumer-name")

    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)

    val expectedFilters = ConsumerFilters(prisons = null, caseNotes = listOf("filter-1", "filter-2"))
    val testRole = Role(include = null, filters = expectedFilters)
    authorisationConfig.consumers = mapOf("consumer-name" to ConsumerConfig(include = null, filters = ConsumerFilters(prisons = null), roles = listOf("test-role")))
    globalsConfig.roles = mapOf("test-role" to testRole)
    every { roles } returns mapOf("test-role" to testRole)

    // Act
    filtersExtractionFilter.doFilter(mockRequest, mockResponse, mockChain)

    // Assert
    verify(mockRequest, times(1)).setAttribute("filters", expectedFilters)
  }

  @Test
  fun `can get case filters attribute from the role and the consumer`() {
    // Arrange
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.getAttribute("clientName")).thenReturn("consumer-name")

    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)

    val expectedFilters = ConsumerFilters(prisons = null, caseNotes = listOf("consumer-filter", "role-filter"))
    val testRole = Role(include = null, filters = ConsumerFilters(prisons = null, caseNotes = listOf("role-filter")))
    authorisationConfig.consumers = mapOf("consumer-name" to ConsumerConfig(include = null, filters = ConsumerFilters(prisons = null, caseNotes = listOf("consumer-filter")), roles = listOf("test-role")))
    globalsConfig.roles = mapOf("test-role" to testRole)
    every { roles } returns mapOf("test-role" to testRole)

    // Act
    filtersExtractionFilter.doFilter(mockRequest, mockResponse, mockChain)

    // Assert
    verify(mockRequest, times(1)).setAttribute("filters", expectedFilters)
  }

  @Test
  fun `can get case filters and prison filters from the role and the consumer`() {
    // Arrange
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.getAttribute("clientName")).thenReturn("consumer-name")

    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)

    val expectedFilters = ConsumerFilters(prisons = listOf("consumer-filter", "role-filter"), caseNotes = listOf("consumer-filter", "role-filter"))
    val testRole = Role(include = null, filters = ConsumerFilters(prisons = listOf("role-filter"), caseNotes = listOf("role-filter")))
    authorisationConfig.consumers = mapOf("consumer-name" to ConsumerConfig(include = null, filters = ConsumerFilters(prisons = listOf("consumer-filter"), caseNotes = listOf("consumer-filter")), roles = listOf("test-role")))
    globalsConfig.roles = mapOf("test-role" to testRole)
    every { roles } returns mapOf("test-role" to testRole)

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
    authorisationConfig.consumers = mapOf("consumer-name" to ConsumerConfig(include = null, ConsumerFilters(null, null), roles = listOf()))

    // Act
    filtersExtractionFilter.doFilter(mockRequest, mockResponse, mockChain)

    // Assert
    verify(mockRequest, times(0)).setAttribute("filters", expectedFilters)
  }
}
