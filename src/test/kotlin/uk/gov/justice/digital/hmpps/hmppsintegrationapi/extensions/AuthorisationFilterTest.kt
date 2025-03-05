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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.RolesConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.Role

class AuthorisationFilterTest {
  private var examplePath: String = "/v1/persons"
  private var authorisationConfig: AuthorisationConfig = AuthorisationConfig()
  private var exampleRoleConfig = RolesConfig(listOf(Role("private-prison", listOf(examplePath))))
  private var authorisationFilter: AuthorisationFilter = AuthorisationFilter(authorisationConfig, exampleRoleConfig)
  private var exampleConsumer: String = "consumer-name"
  private var exampleRoles: List<String> = listOf("private-prison")

  @Test
  fun `calls the onward chain when path found in either`() {
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.requestURI).thenReturn(examplePath)
    whenever(mockRequest.getAttribute("clientName")).thenReturn(exampleConsumer)
    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)

    authorisationConfig.consumers = mapOf(exampleConsumer to ConsumerConfig(include = listOf(examplePath), filters = ConsumerFilters(prisons = null), roles = listOf()))
    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)

    verify(mockChain, times(1)).doFilter(mockRequest, mockResponse)
  }

  @Test
  fun `calls the onward chain when path found in roles (but not in includes)`() {
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.requestURI).thenReturn(examplePath)
    whenever(mockRequest.getAttribute("clientName")).thenReturn(exampleConsumer)
    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)

    authorisationConfig.consumers = mapOf(exampleConsumer to ConsumerConfig(include = emptyList(), filters = ConsumerFilters(prisons = null), roles = exampleRoles))
    val authorisationFilter = AuthorisationFilter(authorisationConfig, exampleRoleConfig)
    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)

    verify(mockChain, times(1)).doFilter(mockRequest, mockResponse)
  }

  @Test
  fun `calls the onward chain when path not found in roles, but found in includes`() {
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.requestURI).thenReturn(examplePath)
    whenever(mockRequest.getAttribute("clientName")).thenReturn(exampleConsumer)
    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)

    authorisationConfig.consumers = mapOf(exampleConsumer to ConsumerConfig(include = listOf(examplePath), filters = ConsumerFilters(prisons = null), roles = listOf()))
    val invalidRoleConfig = RolesConfig(listOf(Role("private-prison", emptyList())))
    val authorisationFilter = AuthorisationFilter(authorisationConfig, invalidRoleConfig)
    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)

    verify(mockChain, times(1)).doFilter(mockRequest, mockResponse)
  }

  @Test
  fun `returns error when the roles or consumer includes do not contain the path`() {
    val invalidPath = "v1/invalid"
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.requestURI).thenReturn(invalidPath)
    whenever(mockRequest.getAttribute("clientName")).thenReturn(exampleConsumer)
    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)

    val authorisationConfig = AuthorisationConfig()
    authorisationConfig.consumers = mapOf(exampleConsumer to ConsumerConfig(include = listOf(examplePath), filters = ConsumerFilters(prisons = null), roles = exampleRoles))
    val authorisationFilter = AuthorisationFilter(authorisationConfig = authorisationConfig, rolesConfig = exampleRoleConfig)
    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)

    verify(mockResponse, times(1)).sendError(403, "Unable to authorise $invalidPath for $exampleConsumer")
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
