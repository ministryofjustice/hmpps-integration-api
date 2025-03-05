package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.RolesConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.Role

class AuthorisationFilterTest {
  private val examplePath: String = "/v1/persons"
  private val roleName = "private-prison"
  private val exampleRoleConfig = RolesConfig(listOf(Role(roleName, listOf(examplePath))))
  private val exampleConsumer: String = "consumer-name"
  private val exampleRoles: List<String> = listOf(roleName)

  private val mockRequest = mock(HttpServletRequest::class.java)
  private val mockResponse = mock(HttpServletResponse::class.java)
  private val mockChain = mock(FilterChain::class.java)

  @BeforeEach
  fun setup() {
    reset(mockRequest)
    reset(mockResponse)
    reset(mockChain)

    whenever(mockRequest.requestURI).thenReturn(examplePath)
    whenever(mockRequest.getAttribute("clientName")).thenReturn(exampleConsumer)
  }

  @Test
  fun `calls the onward chain when path found in either`() {
    val authorisationConfig = AuthorisationConfig()
    authorisationConfig.consumers = mapOf(exampleConsumer to ConsumerConfig(include = listOf(examplePath), filters = ConsumerFilters(prisons = null), roles = listOf()))
    val authorisationFilter = AuthorisationFilter(authorisationConfig, exampleRoleConfig)
    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)

    verify(mockChain, times(1)).doFilter(mockRequest, mockResponse)
  }

  @Test
  fun `calls the onward chain when path found in roles (but not in includes)`() {
    val authorisationConfig = AuthorisationConfig()
    authorisationConfig.consumers = mapOf(exampleConsumer to ConsumerConfig(include = emptyList(), filters = ConsumerFilters(prisons = null), roles = exampleRoles))
    val authorisationFilter = AuthorisationFilter(authorisationConfig, exampleRoleConfig)
    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)

    verify(mockChain, times(1)).doFilter(mockRequest, mockResponse)
  }

  @Test
  fun `calls the onward chain when path not found in roles, but found in includes`() {
    val authorisationConfig = AuthorisationConfig()
    authorisationConfig.consumers = mapOf(exampleConsumer to ConsumerConfig(include = listOf(examplePath), filters = ConsumerFilters(prisons = null), roles = listOf()))
    val invalidRoleConfig = RolesConfig(listOf(Role("private-prison", emptyList())))
    val authorisationFilter = AuthorisationFilter(authorisationConfig, invalidRoleConfig)
    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)

    verify(mockChain, times(1)).doFilter(mockRequest, mockResponse)
  }

  @Test
  fun `returns error when the roles or consumer includes do not contain the path`() {
    val invalidPath = "v1/invalid"
    whenever(mockRequest.requestURI).thenReturn(invalidPath)

    val authorisationConfig = AuthorisationConfig()
    authorisationConfig.consumers = mapOf(exampleConsumer to ConsumerConfig(include = listOf(examplePath), filters = ConsumerFilters(prisons = null), roles = exampleRoles))
    val authorisationFilter = AuthorisationFilter(authorisationConfig = authorisationConfig, rolesConfig = exampleRoleConfig)
    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)

    verify(mockResponse, times(1)).sendError(403, "Unable to authorise $invalidPath for $exampleConsumer")
  }

  @Test
  fun `generates error when subject distinguished name is null in the request`() {
    whenever(mockRequest.getAttribute("clientName")).thenReturn(null)

    val authorisationConfig = AuthorisationConfig()
    val authorisationFilter = AuthorisationFilter(authorisationConfig, exampleRoleConfig)
    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)

    verify(mockResponse, times(1)).sendError(403, "No subject-distinguished-name header provided for authorisation")
  }
}
