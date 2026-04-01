package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
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

  private val roleConfig = ConsumerConfig(roles = listOf("private-prison"), filters = ConsumerFilters(prisons = listOf("MDI")))
  private val defaultFeatureFlags = FeatureFlagConfig(mapOf("normalised-path-matching" to true))

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

  fun mockRequest(
    method: String,
    path: String,
    subjectDistinguishedName: String = "",
    certificateSerialNumber: String = "",
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
    val authConfig = AuthorisationConfig()
    authConfig.consumers = mapOf(consumerName to roleConfig)
    authConfig.certificateRevocationList = certificateRevocationList
    return authConfig
  }

  fun mockFilterChain(vararg filters: Filter): MockFilterChain =
    MockFilterChain(
      mock(HttpServlet::class.java),
      *filters,
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
        AuthorisationFilter(authConfig, defaultFeatureFlags),
        finalFilter,
      )

    chain.doFilter(req, resp)

    verify(finalFilter, times(1)).doFilter(eq(req), eq(resp), any())
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
    val authConfig = mockAuthConfig(exampleConsumer, emptyList())
    val resp = MockHttpServletResponse()
    val req = mockRequest("GET", invalidPath, "", "")
    req.setAttribute("clientName", exampleConsumer)

    val chain = mockFilterChain(AuthorisationFilter(authConfig, defaultFeatureFlags))

    chain.doFilter(req, resp)

    assertThat(resp.status).isEqualTo(403)
    assertThat(resp.errorMessage).contains("Unable to authorise $invalidPath for $exampleConsumer")
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

  @Test
  fun `ConsumerNameExtractionFilter calls the onward chain`() {
    // From ConsumerNameExtractionFilterTest
    val resp = MockHttpServletResponse()
    val finalFilter = mock(Filter::class.java)
    val req = mockRequest("GET", examplePath, "O=test,CN=sam", "9572494320151578633330348943480876283449388176")

    val chain =
      mockFilterChain(
        ConsumerNameExtractionFilter(),
        finalFilter,
      )

    chain.doFilter(req, resp)

    verify(finalFilter, times(1)).doFilter(eq(req), eq(resp), any())
  }

  @Test
  fun `can get subject distinguished name from request and set as consumer name `() {
    // From ConsumerNameExtractionFilterTest
    val resp = MockHttpServletResponse()
    val req = mockRequest("GET", examplePath, "O=test,CN=sam", "9572494320151578633330348943480876283449388176")

    val chain =
      mockFilterChain(
        ConsumerNameExtractionFilter(),
      )

    chain.doFilter(req, resp)

    assertThat(req.getAttribute("clientName")).isEqualTo("sam")
    assertThat(req.getAttribute("certificateSerialNumber")).isEqualTo("01:AD:3E:D8:7D:D5:AA:84:F5:2D:83:E7:87:E9:90:E4:84:C5:2C:90")
  }

  @Test
  fun `does not set a clientName from request if it does not match the regex `() {
    // From ConsumerNameExtractionFilterTest
    val resp = MockHttpServletResponse()
    val req = mockRequest("GET", examplePath, "CN=consumer-name")

    val chain =
      mockFilterChain(
        ConsumerNameExtractionFilter(),
      )

    chain.doFilter(req, resp)

    assertThat(req.getAttribute("clientName")).isNull()
  }

  @Test
  fun `auth filter chain test`() {
    val resp = MockHttpServletResponse()
    val finalFilter = mock(Filter::class.java)
    val authConfig = mockAuthConfig("sam")
    val req = mockRequest("GET", examplePath, "O=test,CN=sam", "9572494320151578633330348943480876283449388176")

    val chain =
      mockFilterChain(
        ConsumerNameExtractionFilter(),
        FiltersExtractionFilter(authConfig),
        AuthorisationFilter(authConfig, defaultFeatureFlags),
        finalFilter,
      )

    chain.doFilter(req, resp)

    assertThat(req.getAttribute("clientName")).isEqualTo("sam")
    assertThat(req.getAttribute("certificateSerialNumber")).isEqualTo("01:AD:3E:D8:7D:D5:AA:84:F5:2D:83:E7:87:E9:90:E4:84:C5:2C:90")
    assertThat(req.getAttribute("filters")).isEqualTo(ConsumerFilters(prisons = listOf("MDI")))
    assertThat(resp.status).isEqualTo(200)
    verify(finalFilter, times(1)).doFilter(eq(req), eq(resp), any())
  }
}
