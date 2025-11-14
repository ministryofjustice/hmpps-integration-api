package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.LimitedAccessException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.Role
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles

class AuthorisationFilterTest {
  private val examplePath: String = "/v1/persons"
  private val roleName = "private-prison"
  private val exampleConsumer: String = "consumer-name"
  private val exampleRoles: List<String> = listOf(roleName)

  private val mockRequest = mock(HttpServletRequest::class.java)
  private val mockResponse = mock(HttpServletResponse::class.java)
  private val mockChain = mock(FilterChain::class.java)
  private val authorisationConfig = mock(AuthorisationConfig::class.java)
  private val featureFlagConfig = mock(FeatureFlagConfig::class.java)

  @BeforeEach
  fun setup() {
    reset(mockRequest)
    reset(mockResponse)
    reset(mockChain)
    reset(featureFlagConfig)
    whenever(mockRequest.requestURI).thenReturn(examplePath)
    whenever(mockRequest.getAttribute("clientName")).thenReturn(exampleConsumer)
    whenever(mockRequest.getAttribute("certificateSerialNumber")).thenReturn("TEST_SERIAL_NUMBER")

    mockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
    every { roles } returns mapOf(roleName to Role(name = "test", permissions = mutableListOf(examplePath), filters = null))
  }

  @AfterEach
  fun after() {
    unmockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
  }

  @Test
  fun `calls the onward chain when path found in either`() {
    whenever(authorisationConfig.consumers).thenReturn(mapOf(exampleConsumer to ConsumerConfig(include = listOf(examplePath), filters = ConsumerFilters(prisons = null), roles = listOf())))
    val authorisationFilter = AuthorisationFilter(authorisationConfig, featureFlagConfig)
    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)

    verify(mockChain, times(1)).doFilter(mockRequest, mockResponse)
  }

  @Test
  fun `calls the onward chain when path found in roles (but not in includes)`() {
    whenever(authorisationConfig.consumers).thenReturn(mapOf(exampleConsumer to ConsumerConfig(include = emptyList(), filters = ConsumerFilters(prisons = null), roles = exampleRoles)))
    val authorisationFilter = AuthorisationFilter(authorisationConfig, featureFlagConfig)
    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)

    verify(mockChain, times(1)).doFilter(mockRequest, mockResponse)
  }

  @Test
  fun `calls the onward chain when path not found in roles, but found in includes`() {
    whenever(authorisationConfig.consumers).thenReturn(mapOf(exampleConsumer to ConsumerConfig(include = listOf(examplePath), filters = ConsumerFilters(prisons = null), roles = listOf())))
    // invalid Role Config
    every { roles } returns mapOf(roleName to Role(permissions = emptyList(), filters = null))
    val authorisationFilter = AuthorisationFilter(authorisationConfig, featureFlagConfig)
    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)

    verify(mockChain, times(1)).doFilter(mockRequest, mockResponse)
  }

  @Test
  fun `returns error when the roles or consumer includes do not contain the path`() {
    val invalidPath = "v1/invalid"
    whenever(mockRequest.requestURI).thenReturn(invalidPath)
    whenever(authorisationConfig.consumers).thenReturn(mapOf(exampleConsumer to ConsumerConfig(include = listOf(examplePath), filters = ConsumerFilters(prisons = null), roles = exampleRoles)))
    val authorisationFilter = AuthorisationFilter(authorisationConfig = authorisationConfig, featureFlagConfig)
    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)

    verify(mockResponse, times(1)).sendError(403, "Unable to authorise $invalidPath for $exampleConsumer")
  }

  @Test
  fun `generates error when subject distinguished name is null in the request`() {
    whenever(mockRequest.getAttribute("clientName")).thenReturn(null)
    val authorisationFilter = AuthorisationFilter(authorisationConfig, featureFlagConfig)
    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)

    verify(mockResponse, times(1)).sendError(403, "No subject-distinguished-name header provided for authorisation")
  }

  @Test
  fun `Forbidden if limited access caused by error for path not found in roles, but found in includes`() {
    whenever(authorisationConfig.consumers).thenReturn(mapOf(exampleConsumer to ConsumerConfig(include = listOf(examplePath), filters = ConsumerFilters(prisons = null), roles = listOf())))
    // invalid Role Config
    every { roles } returns mapOf(roleName to Role(permissions = emptyList(), filters = null))
    val authorisationFilter = AuthorisationFilter(authorisationConfig, featureFlagConfig)
    whenever(mockChain.doFilter(mockRequest, mockResponse)).thenThrow(ServletException(LimitedAccessException()))

    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)

    verify(mockResponse, times(1)).sendError(403, "Attempt to access a limited access case")
  }

  @Test
  fun `Forbidden if limited access caused by error for path found in roles (but not in includes)`() {
    whenever(authorisationConfig.consumers).thenReturn(mapOf(exampleConsumer to ConsumerConfig(include = emptyList(), filters = ConsumerFilters(prisons = null), roles = exampleRoles)))
    val authorisationFilter = AuthorisationFilter(authorisationConfig, featureFlagConfig)
    whenever(mockChain.doFilter(mockRequest, mockResponse)).thenThrow(ServletException(LimitedAccessException()))

    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)

    verify(mockResponse, times(1)).sendError(403, "Attempt to access a limited access case")
  }

  @Test
  fun `Forbidden if certificate serial number is in the certificate revocation list and feature flag is enabled`() {
    whenever(authorisationConfig.certificateRevocationList).thenReturn(listOf("TEST_SERIAL_NUMBER", "TEST_SERIAL_NUMBER_2"))
    val authorisationFilter = AuthorisationFilter(authorisationConfig, featureFlagConfig)
    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)
    verify(mockResponse, times(1)).sendError(403, "Certificate with serial number TEST_SERIAL_NUMBER has been revoked")
  }

  @Test
  fun `NOT Forbidden if certificate serial number is in the certificate revocation list and feature flag is enabled`() {
    whenever(authorisationConfig.certificateRevocationList).thenReturn(listOf("TEST_SERIAL_NUMBER_3", "TEST_SERIAL_NUMBER_4"))
    val authorisationFilter = AuthorisationFilter(authorisationConfig, featureFlagConfig)
    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)
    verify(mockResponse, times(0)).sendError(403, "Certificate with serial number TEST_SERIAL_NUMBER has been revoked")
  }
}
