package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.Config
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.LimitedAccessException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.Role
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.RoleService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService

private const val CERT_SERIAL_RAW = "9572494320151578633330348943480876283449388176"
private const val CERT_SERIAL_FORMATTED = "01:AD:3E:D8:7D:D5:AA:84:F5:2D:83:E7:87:E9:90:E4:84:C5:2C:90"

class AuthorisationFilterTest {
  private val examplePath: String = "/v1/persons"
  private val roleName = "private-prison"
  private val exampleConsumer: String = "consumer-name"
  private val exampleSubjectDistinguishedName = "C=GB,ST=London,L=London,O=Home Office,CN=$exampleConsumer"
  private val exampleRoles: List<String> = listOf(roleName)

  private val mockRequest = mock(HttpServletRequest::class.java)
  private val mockResponse = mock(HttpServletResponse::class.java)
  private val mockChain = mock(FilterChain::class.java)
  private val authorisationConfig = mock(AuthorisationConfig::class.java)
  private val featureFlagConfig = mock(FeatureFlagConfig::class.java)
  private val mockRoleService = mock(RoleService::class.java)
  val mockTelemetryService = mock(TelemetryService::class.java)

  private val roleConfig = ConsumerConfig(roles = listOf("private-prison"), filters = ConsumerFilters(prisons = listOf("MDI")))

  @BeforeEach
  fun setup() {
    reset(mockRequest)
    reset(mockResponse)
    reset(mockChain)
    reset(featureFlagConfig)
    whenever(mockRequest.requestURI).thenReturn(examplePath)
    whenever(mockRequest.getHeader("subject-distinguished-name")).thenReturn(exampleSubjectDistinguishedName)
    whenever(mockRequest.getHeader("cert-serial-number")).thenReturn(CERT_SERIAL_RAW)
    whenever(mockRequest.getHeader("X-On-Behalf-Of")).thenReturn("TEST_BEHALF_OF")
    whenever(mockRoleService.getRoles()).thenReturn(mapOf(roleName to Role(name = "test", permissions = mutableListOf(examplePath), filters = null)))
  }

  fun mockRequest(
    method: String,
    path: String,
    subjectDistinguishedName: String = exampleSubjectDistinguishedName,
    certificateSerialNumber: String = CERT_SERIAL_RAW,
  ): HttpServletRequest {
    val req = MockHttpServletRequest(method, path)
    req.addHeader("subject-distinguished-name", subjectDistinguishedName)
    req.addHeader("cert-serial-number", certificateSerialNumber)
    return req
  }

  fun mockAuthConfig(
    consumerName: String,
    certificateRevocationList: List<String> = emptyList(),
  ): AuthorisationConfig {
    val authConfig = AuthorisationConfig(mockRoleService, Config(mapOf(consumerName to roleConfig), certificateRevocationList))
    return authConfig
  }

  fun mockFilterChain(vararg filters: Filter): MockFilterChain =
    MockFilterChain(
      mock(HttpServlet::class.java),
      *filters,
    )

  fun fullMockFilterChain(
    authConfig: AuthorisationConfig,
    finalFilter: Filter = mock(Filter::class.java),
  ): MockFilterChain =
    mockFilterChain(
      AuthorisationFilter(authConfig, mockTelemetryService, mockRoleService),
      finalFilter,
    )

  @Test
  fun `calls the onward chain when path found in either`() {
    val resp = MockHttpServletResponse()
    val finalFilter = mock(Filter::class.java)
    val authConfig = mockAuthConfig(exampleConsumer)
    val req = mockRequest("GET", "/v1/persons")
    req.setAttribute("clientName", exampleConsumer)

    val chain =
      mockFilterChain(
        AuthorisationFilter(authConfig, mockTelemetryService, mockRoleService),
        finalFilter,
      )

    chain.doFilter(req, resp)

    verify(finalFilter, times(1)).doFilter(eq(req), eq(resp), any())
  }

  @Test
  fun `calls the onward chain when path found in roles (but not in includes)`() {
    whenever(authorisationConfig.consumers).thenReturn(mapOf(exampleConsumer to ConsumerConfig(include = emptyList(), filters = ConsumerFilters(prisons = null), roles = exampleRoles)))
    val authorisationFilter = AuthorisationFilter(authorisationConfig, mockTelemetryService, mockRoleService)
    val finalFilter = mock(Filter::class.java)

    mockFilterChain(authorisationFilter, finalFilter).doFilter(mockRequest, mockResponse)

    verify(finalFilter, times(1)).doFilter(eq(mockRequest), eq(mockResponse), any())
  }

  @Test
  fun `calls the onward chain when path not found in roles, but found in includes`() {
    whenever(authorisationConfig.consumers).thenReturn(mapOf(exampleConsumer to ConsumerConfig(include = listOf(examplePath), filters = ConsumerFilters(prisons = null), roles = listOf())))
    // invalid Role Config
    whenever(mockRoleService.getRoles()).thenReturn(mapOf(roleName to Role(permissions = emptyList(), filters = null)))
    val authorisationFilter = AuthorisationFilter(authorisationConfig, mockTelemetryService, mockRoleService)
    val finalFilter = mock(Filter::class.java)

    mockFilterChain(authorisationFilter, finalFilter).doFilter(mockRequest, mockResponse)

    verify(finalFilter, times(1)).doFilter(eq(mockRequest), eq(mockResponse), any())
  }

  @Test
  fun `returns error when the roles or consumer includes do not contain the path`() {
    val invalidPath = "v1/invalid"
    val authConfig = mockAuthConfig(exampleConsumer, emptyList())
    val resp = MockHttpServletResponse()
    val req = mockRequest("GET", invalidPath)
    req.setAttribute("clientName", exampleConsumer)

    val chain = mockFilterChain(AuthorisationFilter(authConfig, mockTelemetryService, mockRoleService))

    chain.doFilter(req, resp)

    assertThat(resp.status).isEqualTo(403)
    assertThat(resp.errorMessage).contains("Unable to authorise $invalidPath for $exampleConsumer")
  }

  @Test
  fun `generates error when subject distinguished name is null in the request`() {
    whenever(mockRequest.getHeader("subject-distinguished-name")).thenReturn(null)
    val authorisationFilter = AuthorisationFilter(authorisationConfig, mockTelemetryService, mockRoleService)
    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)

    verify(mockResponse, times(1)).sendError(403, "No subject-distinguished-name header provided for authorisation")
  }

  @Test
  fun `Forbidden if limited access caused by error for path not found in roles, but found in includes`() {
    whenever(authorisationConfig.consumers).thenReturn(mapOf(exampleConsumer to ConsumerConfig(include = listOf(examplePath), filters = ConsumerFilters(prisons = null), roles = listOf())))
    // invalid Role Config
    whenever(mockRoleService.getRoles()).thenReturn(mapOf(roleName to Role(permissions = emptyList(), filters = null)))
    val authorisationFilter = AuthorisationFilter(authorisationConfig, mockTelemetryService, mockRoleService)
    whenever(mockChain.doFilter(mockRequest, mockResponse)).thenThrow(ServletException(LimitedAccessException()))

    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)

    verify(mockResponse, times(1)).sendError(403, "Attempt to access a limited access case")
  }

  @Test
  fun `Forbidden if limited access caused by error for path found in roles (but not in includes)`() {
    whenever(authorisationConfig.consumers).thenReturn(mapOf(exampleConsumer to ConsumerConfig(include = emptyList(), filters = ConsumerFilters(prisons = null), roles = exampleRoles)))
    val authorisationFilter = AuthorisationFilter(authorisationConfig, mockTelemetryService, mockRoleService)
    whenever(mockChain.doFilter(mockRequest, mockResponse)).thenThrow(ServletException(LimitedAccessException()))

    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)

    verify(mockResponse, times(1)).sendError(403, "Attempt to access a limited access case")
  }

  @Test
  fun `Forbidden if certificate serial number is in the certificate revocation list and feature flag is enabled`() {
    whenever(authorisationConfig.certificateRevocationList).thenReturn(listOf(CERT_SERIAL_FORMATTED, "TEST_SERIAL_NUMBER_2"))
    whenever(authorisationConfig.consumers).thenReturn(mapOf(exampleConsumer to ConsumerConfig(include = emptyList(), filters = ConsumerFilters(prisons = null), roles = exampleRoles)))
    val resp = MockHttpServletResponse()
    val authorisationFilter = AuthorisationFilter(authorisationConfig, mockTelemetryService, mockRoleService)
    mockFilterChain(authorisationFilter).doFilter(mockRequest, resp)
    assertThat(resp.status).isEqualTo(403)
    assertThat(resp.errorMessage).isEqualTo("Certificate with serial number 01:AD:3E:D8:7D:D5:AA:84:F5:2D:83:E7:87:E9:90:E4:84:C5:2C:90 has been revoked")
  }

  @Test
  fun `NOT Forbidden if certificate serial number is in the certificate revocation list and feature flag is enabled`() {
    whenever(authorisationConfig.certificateRevocationList).thenReturn(listOf("TEST_SERIAL_NUMBER_3", "TEST_SERIAL_NUMBER_4"))
    val resp = MockHttpServletResponse()
    val authorisationFilter = AuthorisationFilter(authorisationConfig, mockTelemetryService, mockRoleService)
    mockFilterChain(authorisationFilter).doFilter(mockRequest, resp)
    assertThat(resp.errorMessage).isNotEqualTo("Certificate with serial number 01:AD:3E:D8:7D:D5:AA:84:F5:2D:83:E7:87:E9:90:E4:84:C5:2C:90 has been revoked")
  }

  @Test
  fun `can get subject distinguished name from request and set as consumer name `() {
    // From ConsumerNameExtractionFilterTest
    val resp = MockHttpServletResponse()
    val req = mockRequest("GET", examplePath, "O=test,CN=sam", CERT_SERIAL_RAW)

    val chain = fullMockFilterChain(authorisationConfig)

    chain.doFilter(req, resp)

    assertThat(req.getAttribute("clientName")).isEqualTo("sam")
  }

  @Test
  fun `does not set a clientName from request if it does not match the regex `() {
    // From ConsumerNameExtractionFilterTest
    val resp = MockHttpServletResponse()
    val req = mockRequest("GET", examplePath, "CN=consumer-name")

    val chain = fullMockFilterChain(authorisationConfig)

    chain.doFilter(req, resp)

    assertThat(req.getAttribute("clientName")).isNull()
  }

  @Test
  fun `can get prison filters attribute from the consumer config`() {
    // From FiltersExtractionFilterTest
    val resp = MockHttpServletResponse()

    val config = ConsumerConfig(include = null, filters = ConsumerFilters(prisons = listOf("A", "B")), roles = listOf())
    val authConfig = AuthorisationConfig(mockRoleService, Config(mapOf("consumer-name" to config)))

    val req = mockRequest("GET", examplePath, "O=test,CN=consumer-name")

    val chain = fullMockFilterChain(authConfig)

    chain.doFilter(req, resp)

    assertThat(req.getAttribute("filters")).isEqualTo(ConsumerFilters(prisons = listOf("A", "B")))
  }

  @Test
  fun `can get prison filters attribute from the role`() {
    // From FiltersExtractionFilterTest
    val testRole = Role(filters = ConsumerFilters(prisons = listOf("RolePrison")))
    whenever(mockRoleService.getRoles()).thenReturn(mapOf("test-role" to testRole))

    val resp = MockHttpServletResponse()

    val consumerConfig = ConsumerConfig(include = null, filters = null, roles = listOf("test-role"))
    val authConfig = AuthorisationConfig(mockRoleService, Config(mapOf("consumer-name" to consumerConfig)))

    val req = mockRequest("GET", examplePath, "O=test,CN=consumer-name")

    val chain = fullMockFilterChain(authConfig)

    chain.doFilter(req, resp)

    assertThat(req.getAttribute("filters")).isEqualTo(ConsumerFilters(prisons = listOf("RolePrison")))
  }

  @Test
  fun `can get prison filters attribute from the role and the consumer`() {
    // From FiltersExtractionFilterTest
    val testRole = Role(filters = ConsumerFilters(prisons = listOf("RolePrison")))
    whenever(mockRoleService.getRoles()).thenReturn(mapOf("test-role" to testRole))

    val resp = MockHttpServletResponse()
    val authConfig = AuthorisationConfig(mockRoleService, Config(mapOf("consumer-name" to ConsumerConfig(include = null, filters = ConsumerFilters(prisons = listOf("MDI")), roles = listOf("test-role")))))

    val req = mockRequest("GET", examplePath, "O=test,CN=consumer-name")

    val chain = fullMockFilterChain(authConfig)

    chain.doFilter(req, resp)

    assertThat(req.getAttribute("filters")).isEqualTo(ConsumerFilters(prisons = listOf("MDI", "RolePrison")))
  }

  @Test
  fun `auth filter chain test`() {
    val resp = MockHttpServletResponse()
    val finalFilter = mock(Filter::class.java)
    val authConfig = mockAuthConfig("sam")
    val req = mockRequest("GET", examplePath, "O=test,CN=sam", CERT_SERIAL_RAW)

    val chain = fullMockFilterChain(authConfig, finalFilter)

    chain.doFilter(req, resp)

    assertThat(req.getAttribute("clientName")).isEqualTo("sam")
    assertThat(req.getAttribute("filters")).isEqualTo(ConsumerFilters(prisons = listOf("MDI")))
    assertThat(resp.status).isEqualTo(200)
    verify(finalFilter, times(1)).doFilter(eq(req), eq(resp), any())
  }

  // App insights request span attribute tests

  @Test
  fun `handles clientName attribute`() {
    val authorisationFilter = AuthorisationFilter(authorisationConfig, mockTelemetryService, mockRoleService)
    val finalFilter = mock(Filter::class.java)
    mockFilterChain(authorisationFilter, finalFilter).doFilter(mockRequest, mockResponse)
    verify(mockTelemetryService, times(1)).setSpanAttribute("clientId", exampleConsumer)
  }

  @Test
  fun `handles missing clientName attribute`() {
    whenever(mockRequest.getHeader("subject-distinguished-name")).thenReturn(null)
    val authorisationFilter = AuthorisationFilter(authorisationConfig, mockTelemetryService, mockRoleService)
    val finalFilter = mock(Filter::class.java)
    mockFilterChain(authorisationFilter, finalFilter).doFilter(mockRequest, mockResponse)
    verify(mockTelemetryService, times(0)).setSpanAttribute("clientId", exampleConsumer)
  }

  @Test
  fun `handles a certificate serial number header`() {
    val authorisationFilter = AuthorisationFilter(authorisationConfig, mockTelemetryService, mockRoleService)
    val finalFilter = mock(Filter::class.java)
    mockFilterChain(authorisationFilter, finalFilter).doFilter(mockRequest, mockResponse)
    verify(mockTelemetryService, times(1)).setSpanAttribute("certSerialNumber", CERT_SERIAL_FORMATTED)
  }

  @Test
  fun `handles a NULL certificate serial number header`() {
    whenever(mockRequest.getHeader("cert-serial-number")).thenReturn(null)
    val authorisationFilter = AuthorisationFilter(authorisationConfig, mockTelemetryService, mockRoleService)
    val finalFilter = mock(Filter::class.java)
    mockFilterChain(authorisationFilter, finalFilter).doFilter(mockRequest, mockResponse)
    verify(mockTelemetryService, times(0)).setSpanAttribute("certSerialNumber", CERT_SERIAL_FORMATTED)
  }

  @Test
  fun `handles a on behalf of header`() {
    val authorisationFilter = AuthorisationFilter(authorisationConfig, mockTelemetryService, mockRoleService)
    val finalFilter = mock(Filter::class.java)
    mockFilterChain(authorisationFilter, finalFilter).doFilter(mockRequest, mockResponse)
    verify(mockTelemetryService, times(1)).setSpanAttribute("onBehalfOf", "TEST_BEHALF_OF")
  }

  @Test
  fun `handles a NULL on behalf of header`() {
    whenever(mockRequest.getHeader("X-On-Behalf-Of")).thenReturn(null)
    val authorisationFilter = AuthorisationFilter(authorisationConfig, mockTelemetryService, mockRoleService)
    val finalFilter = mock(Filter::class.java)
    mockFilterChain(authorisationFilter, finalFilter).doFilter(mockRequest, mockResponse)
    verify(mockTelemetryService, times(0)).setSpanAttribute("onBehalfOf", "TEST_BEHALF_OF")
  }
}
