package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_ROLES_DSL
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.GlobalsConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.Role
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
class FiltersExtractionFilterRoleDslTest {
  @Autowired
  lateinit var authorisationConfig: AuthorisationConfig

  @Autowired
  lateinit var globalsConfig: GlobalsConfig

  @Autowired
  lateinit var featureFlagConfig: FeatureFlagConfig

  companion object {
    @JvmStatic
    fun roleArguments() =
      listOf(
        Arguments.of(true, "full-access", null),
        Arguments.of(false, "full-access", null),
        Arguments.of(true, "private-prison", null),
        Arguments.of(false, "private-prison", null),
        Arguments.of(true, "police", null),
        Arguments.of(false, "police", null),
        Arguments.of(true, "curious", null),
        Arguments.of(false, "curious", null),
        Arguments.of(true, "reference-data-only", null),
        Arguments.of(false, "reference-data-only", null),
        Arguments.of(true, "prisoner-escort-custody-service", ConsumerFilters(caseNotes = listOf("CAB", "NEG", "CVM", "INTERVENTION", "POS"), prisons = emptyList())),
        Arguments.of(false, "prisoner-escort-custody-service", ConsumerFilters(caseNotes = listOf("CAB", "NEG", "CVM", "INTERVENTION", "POS"), prisons = emptyList())),
        Arguments.of(true, "mappa", null),
        Arguments.of(false, "mappa", null),
        Arguments.of(true, "all-endpoints", null),
        Arguments.of(false, "all-endpoints", null),
      )
  }

  @ParameterizedTest
  @MethodSource("roleArguments")
  fun `amends the consumer filters based on role in the same way for Globals config and DSL`(
    useDsl: Boolean,
    roleName: String,
    expectedFilters: ConsumerFilters?,
  ) {
    val filtersExtractionFilter =
      FiltersExtractionFilter(
        authorisationConfig,
        globalsConfig,
        FeatureFlagConfig(mapOf(USE_ROLES_DSL to useDsl)),
      )
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.getAttribute("clientName")).thenReturn("consumer-name")
    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)
    val filtersCapture = ArgumentCaptor.forClass(ConsumerFilters::class.java)
    val roleFilters = roles[roleName]?.filters
    val testRole = Role(include = null, filters = roleFilters)
    authorisationConfig.consumers = mapOf("consumer-name" to ConsumerConfig(include = null, filters = ConsumerFilters(prisons = null), roles = listOf("test-role")))
    globalsConfig.roles = mapOf("test-role" to testRole)
    filtersExtractionFilter.roles = mapOf("test-role" to testRole)
    filtersExtractionFilter.doFilter(mockRequest, mockResponse, mockChain)
    verify(mockRequest, times(1)).setAttribute(eq("filters"), filtersCapture.capture())
    val actualFilters = filtersCapture?.value
    assertThat(actualFilters).isEqualTo(expectedFilters)
  }
}
