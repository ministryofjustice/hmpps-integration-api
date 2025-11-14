package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.FiltersExtractionFilter
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.Role
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.police

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
class FiltersExtractionFilterIntegrationRoleDslTest {
  @MockitoBean
  lateinit var authorisationConfig: AuthorisationConfig

  @MockitoBean
  lateinit var featureFlagConfig: FeatureFlagConfig

  @BeforeEach
  fun setUp() {
    mockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
  }

  @AfterEach
  fun after() {
    unmockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
  }

  companion object {
    @JvmStatic
    fun roleArguments() =
      listOf(
        Arguments.of("full-access", null),
        Arguments.of("private-prison", null),
        Arguments.of("police", ConsumerFilters(alertCodes = police.filters?.alertCodes!!)),
        Arguments.of("curious", null),
        Arguments.of("reference-data-only", null),
        Arguments.of("prisoner-escort-custody-service", ConsumerFilters(caseNotes = listOf("CAB", "NEG", "CVM", "INTERVENTION", "POS"), prisons = null)),
        Arguments.of("mappa", null),
        Arguments.of("all-endpoints", null),
      )
  }

  @ParameterizedTest
  @MethodSource("roleArguments")
  fun `amends the consumer filters based on role in the same way for Globals config and DSL`(
    roleName: String,
    expectedFilters: ConsumerFilters?,
  ) {
    val filtersExtractionFilter =
      FiltersExtractionFilter(
        authorisationConfig,
      )
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.getAttribute("clientName")).thenReturn("consumer-name")
    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)
    val filtersCapture = ArgumentCaptor.forClass(ConsumerFilters::class.java)
    val roleFilters = roles[roleName]?.filters
    val testRole = Role(permissions = null, filters = roleFilters)
    whenever(authorisationConfig.allFilters(any())).thenCallRealMethod()
    whenever(authorisationConfig.consumers).thenReturn(mapOf("consumer-name" to ConsumerConfig(include = null, filters = ConsumerFilters(prisons = null), roles = listOf("test-role"))))
    every { roles } returns mapOf("test-role" to testRole)
    filtersExtractionFilter.doFilter(mockRequest, mockResponse, mockChain)
    verify(mockRequest, times(1)).setAttribute(eq("filters"), filtersCapture.capture())
    val actualFilters = filtersCapture?.value
    assertThat(actualFilters).isEqualTo(expectedFilters)
  }
}
