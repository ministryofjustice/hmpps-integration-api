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

class RoleExtractionFilterTest {
  private var examplePath: String = "/v1/persons"
  private var exampleConsumer: String = "consumer-name"
  private var exampleRoles: List<String> = listOf("private-prison")
  private var exampleRoleConfig = RolesConfig(listOf(Role("private-prison", listOf(examplePath))))

  @Test
  fun `calls the onward chain`() {
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.requestURI).thenReturn(examplePath)
    whenever(mockRequest.getAttribute("clientName")).thenReturn(exampleConsumer)
    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)

    val authorisationConfig = AuthorisationConfig()
    authorisationConfig.consumers = mapOf(exampleConsumer to ConsumerConfig(include = listOf(examplePath), filters = ConsumerFilters(prisons = null), roles = exampleRoles))
    val roleFilter = RoleExtractionFilter(rolesConfig = exampleRoleConfig, authorisationConfig = authorisationConfig)
    roleFilter.doFilter(mockRequest, mockResponse, mockChain)

    verify(mockChain, times(1)).doFilter(mockRequest, mockResponse)
  }

  @Test
  fun `returns error when the roles do not contain the path`() {
    val invalidPath = "v1/invalid"
    val mockRequest = mock(HttpServletRequest::class.java)
    whenever(mockRequest.requestURI).thenReturn(invalidPath)
    whenever(mockRequest.getAttribute("clientName")).thenReturn(exampleConsumer)
    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)

    val authorisationConfig = AuthorisationConfig()
    authorisationConfig.consumers = mapOf(exampleConsumer to ConsumerConfig(include = listOf(examplePath), filters = ConsumerFilters(prisons = null), roles = exampleRoles))
    val roleFilter = RoleExtractionFilter(rolesConfig = exampleRoleConfig, authorisationConfig = authorisationConfig)
    roleFilter.doFilter(mockRequest, mockResponse, mockChain)

    verify(mockResponse, times(1)).sendError(403, "Unable to authorise $invalidPath for $exampleConsumer")
  }
}
