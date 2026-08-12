package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.AuthorisationService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.CertificateInfo
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.CertificateService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.ManageUsersService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.onbehalfof.OboService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import java.time.Instant

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
  private val authorisationService = mock(AuthorisationService::class.java)
  private val featureFlagConfig = mock(FeatureFlagConfig::class.java)
  private val mockTelemetryService = mock(TelemetryService::class.java)
  private val mockManageUsersService = mock(ManageUsersService::class.java)
  private val certificateService = mock(CertificateService::class.java)
  private val roleConfig = ConsumerConfig(roles = listOf("private-prison"), filters = ConsumerFilters(prisons = listOf("MDI")))
  private val authorisationFilter = AuthorisationFilter(authorisationService, mockTelemetryService, featureFlagConfig, certificateService)

  @BeforeEach
  fun setup() {
    reset(mockRequest)
    reset(mockResponse)
    reset(mockChain)
    reset(featureFlagConfig)
    reset(certificateService)
    whenever(mockRequest.requestURI).thenReturn(examplePath)
    whenever(mockRequest.getHeader("subject-distinguished-name")).thenReturn(exampleSubjectDistinguishedName)
    whenever(mockRequest.getHeader("cert-serial-number")).thenReturn(CERT_SERIAL_RAW)
    whenever(mockRequest.getHeader("cert-expiry-date")).thenReturn("May 20 00:30:10 2046 GMT")
    val certInfo = CertificateInfo(CERT_SERIAL_FORMATTED, exampleConsumer, false, Instant.now().plusSeconds(3600))
    whenever(certificateService.getCertificateInfo(any(), any(), any())).thenReturn(certInfo)
    whenever(certificateService.extractConsumerName(eq(exampleSubjectDistinguishedName))).thenReturn(exampleConsumer)
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
    req.addHeader("cert-expiry-date", "May 20 00:30:10 2046 GMT")
    return req
  }

  fun mockAuthService(
    consumerName: String,
    certificateRevocationList: List<String> = emptyList(),
  ): AuthorisationService {
    val authService =
      AuthorisationService(
        AuthorisationConfig(
          mapOf(consumerName to roleConfig),
          certificateRevocationList,
          roles = mapOf(roleName to Role(name = "test", permissions = mutableListOf(examplePath), filters = null)),
        ),
        mockTelemetryService,
        mockManageUsersService,
      )
    return authService
  }

  fun mockFilterChain(vararg filters: Filter): MockFilterChain =
    MockFilterChain(
      mock(HttpServlet::class.java),
      *filters,
    )

  fun fullMockFilterChain(
    authService: AuthorisationService,
    finalFilter: Filter = mock(Filter::class.java),
  ): MockFilterChain =
    mockFilterChain(
      AuthorisationFilter(authService, mockTelemetryService, featureFlagConfig, certificateService),
      finalFilter,
    )

  @Test
  fun `calls the onward chain when path found in either`() {
    val resp = MockHttpServletResponse()
    val finalFilter = mock(Filter::class.java)
    val authService = mockAuthService(exampleConsumer)
    val req = mockRequest("GET", "/v1/persons")
    req.setAttribute("clientName", exampleConsumer)

    val chain =
      mockFilterChain(
        AuthorisationFilter(authService, mockTelemetryService, featureFlagConfig, certificateService),
        finalFilter,
      )

    chain.doFilter(req, resp)

    verify(finalFilter, times(1)).doFilter(eq(req), eq(resp), any())
  }

  @Test
  fun `calls the onward chain when path found in roles (but not in includes)`() {
    val authorisationService =
      AuthorisationService(
        AuthorisationConfig(
          mapOf(exampleConsumer to ConsumerConfig(include = emptyList(), filters = ConsumerFilters(prisons = null), roles = exampleRoles)),
          roles = mapOf(roleName to Role(name = "test", permissions = mutableListOf(examplePath), filters = null)),
        ),
        mockTelemetryService,
        mockManageUsersService,
      )
    val authorisationFilter = AuthorisationFilter(authorisationService, mockTelemetryService, featureFlagConfig, certificateService)
    val finalFilter = mock(Filter::class.java)

    mockFilterChain(authorisationFilter, finalFilter).doFilter(mockRequest, mockResponse)

    verify(finalFilter, times(1)).doFilter(eq(mockRequest), eq(mockResponse), any())
  }

  @Test
  fun `calls the onward chain when path not found in roles, but found in includes`() {
    val authorisationService =
      AuthorisationService(
        AuthorisationConfig(
          mapOf(exampleConsumer to ConsumerConfig(include = emptyList(), filters = ConsumerFilters(prisons = null), roles = exampleRoles)),
          roles = mapOf(roleName to Role(name = "test", permissions = mutableListOf(examplePath), filters = null)),
        ),
        mockTelemetryService,
        mockManageUsersService,
      )
    // invalid Role Config
    val authorisationFilter = AuthorisationFilter(authorisationService, mockTelemetryService, featureFlagConfig, certificateService)
    val finalFilter = mock(Filter::class.java)

    mockFilterChain(authorisationFilter, finalFilter).doFilter(mockRequest, mockResponse)

    verify(finalFilter, times(1)).doFilter(eq(mockRequest), eq(mockResponse), any())
  }

  @Test
  fun `returns error when the roles or consumer includes do not contain the path`() {
    val invalidPath = "v1/invalid"
    val authService = mockAuthService(exampleConsumer, emptyList())
    val resp = MockHttpServletResponse()
    val req = mockRequest("GET", invalidPath)
    req.setAttribute("clientName", exampleConsumer)

    val chain = mockFilterChain(AuthorisationFilter(authService, mockTelemetryService, featureFlagConfig, certificateService))

    chain.doFilter(req, resp)

    assertThat(resp.status).isEqualTo(403)
    assertThat(resp.errorMessage).contains("Unable to authorise $invalidPath for $exampleConsumer")
  }

  @Test
  fun `generates error when subject distinguished name is null in the request`() {
    whenever(mockRequest.getHeader("subject-distinguished-name")).thenReturn(null)
    val authorisationFilter = AuthorisationFilter(authorisationService, mockTelemetryService, featureFlagConfig, certificateService)
    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)

    verify(mockResponse, times(1)).sendError(403, "No subject-distinguished-name header provided for authorisation")
  }

  @Test
  fun `Forbidden if limited access caused by error for path not found in roles, but found in includes`() {
    val authorisationService =
      AuthorisationService(
        AuthorisationConfig(
          mapOf(exampleConsumer to ConsumerConfig(include = emptyList(), filters = ConsumerFilters(prisons = null), roles = exampleRoles)),
          roles = mapOf(roleName to Role(name = "test", permissions = mutableListOf(examplePath), filters = null)),
        ),
        mockTelemetryService,
        mockManageUsersService,
      )
    // invalid Role Config
    val authorisationFilter = AuthorisationFilter(authorisationService, mockTelemetryService, featureFlagConfig, certificateService)
    whenever(mockChain.doFilter(mockRequest, mockResponse)).thenThrow(ServletException(LimitedAccessException()))

    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)

    verify(mockResponse, times(1)).sendError(403, "Attempt to access a limited access case")
  }

  @Test
  fun `Forbidden if limited access caused by error for path found in roles (but not in includes)`() {
    val authorisationService =
      AuthorisationService(
        AuthorisationConfig(
          mapOf(exampleConsumer to ConsumerConfig(include = emptyList(), filters = ConsumerFilters(prisons = null), roles = exampleRoles)),
          roles = mapOf(roleName to Role(name = "test", permissions = mutableListOf(examplePath), filters = null)),
        ),
        mockTelemetryService,
        mockManageUsersService,
      )
    val authorisationFilter = AuthorisationFilter(authorisationService, mockTelemetryService, featureFlagConfig, certificateService)
    whenever(mockChain.doFilter(mockRequest, mockResponse)).thenThrow(ServletException(LimitedAccessException()))

    authorisationFilter.doFilter(mockRequest, mockResponse, mockChain)

    verify(mockResponse, times(1)).sendError(403, "Attempt to access a limited access case")
  }

  @Test
  fun `Forbidden if certificate serial number is in the certificate revocation list and feature flag is enabled`() {
    whenever(certificateService.getCertificateInfo(any(), any(), any())).thenReturn(
      CertificateInfo(
        CERT_SERIAL_FORMATTED,
        exampleConsumer,
        true,
        null,
      ),
    )
    whenever(authorisationService.consumers()).thenReturn(mapOf(exampleConsumer to ConsumerConfig(include = emptyList(), filters = ConsumerFilters(prisons = null), roles = exampleRoles)))
    val resp = MockHttpServletResponse()
    val authorisationFilter = AuthorisationFilter(authorisationService, mockTelemetryService, featureFlagConfig, certificateService)
    mockFilterChain(authorisationFilter).doFilter(mockRequest, resp)
    assertThat(resp.status).isEqualTo(403)
    assertThat(resp.errorMessage).isEqualTo("Certificate with serial number 01:AD:3E:D8:7D:D5:AA:84:F5:2D:83:E7:87:E9:90:E4:84:C5:2C:90 has been revoked")
  }

  @Test
  fun `NOT Forbidden if certificate serial number is in the certificate revocation list and feature flag is enabled`() {
    whenever(certificateService.certificateRevocationList()).thenReturn(listOf("TEST_SERIAL_NUMBER_3", "TEST_SERIAL_NUMBER_4"))
    val resp = MockHttpServletResponse()
    mockFilterChain(authorisationFilter).doFilter(mockRequest, resp)
    assertThat(resp.errorMessage).isNotEqualTo("Certificate with serial number 01:AD:3E:D8:7D:D5:AA:84:F5:2D:83:E7:87:E9:90:E4:84:C5:2C:90 has been revoked")
  }

  @Test
  fun `can get subject distinguished name from request and set as consumer name `() {
    // From ConsumerNameExtractionFilterTest
    val resp = MockHttpServletResponse()
    val req = mockRequest("GET", examplePath)

    val chain = fullMockFilterChain(authorisationService)

    chain.doFilter(req, resp)

    assertThat(req.getAttribute("clientName")).isEqualTo("consumer-name")
  }

  @Test
  fun `does not set a clientName from request if it does not match the regex `() {
    // From ConsumerNameExtractionFilterTest
    val resp = MockHttpServletResponse()
    val req = mockRequest("GET", examplePath, "CN=consumer-name")

    val chain = fullMockFilterChain(authorisationService)

    chain.doFilter(req, resp)

    assertThat(req.getAttribute("clientName")).isNull()
  }

  @Test
  fun `can get prison filters attribute from the consumer config`() {
    // From FiltersExtractionFilterTest
    val resp = MockHttpServletResponse()

    val config = ConsumerConfig(include = null, filters = ConsumerFilters(prisons = listOf("A", "B")), roles = listOf())
    val authorisationService =
      AuthorisationService(
        AuthorisationConfig(mapOf("consumer-name" to config)),
        mockTelemetryService,
        mockManageUsersService,
      )

    val req = mockRequest("GET", examplePath)

    val chain = fullMockFilterChain(authorisationService)

    chain.doFilter(req, resp)

    assertThat(req.getAttribute("filters")).isEqualTo(ConsumerFilters(prisons = listOf("A", "B")))
  }

  @Test
  fun `can get prison filters attribute from the role`() {
    // From FiltersExtractionFilterTest
    val testRole = Role(filters = ConsumerFilters(prisons = listOf("RolePrison")))

    val resp = MockHttpServletResponse()

    val consumerConfig = ConsumerConfig(include = null, filters = null, roles = listOf("test-role"))
    val authorisationService =
      AuthorisationService(
        AuthorisationConfig(
          mapOf("consumer-name" to consumerConfig),
          roles = mapOf("test-role" to testRole),
        ),
        mockTelemetryService,
        mockManageUsersService,
      )

    val req = mockRequest("GET", examplePath)

    val chain = fullMockFilterChain(authorisationService)

    chain.doFilter(req, resp)

    assertThat(req.getAttribute("filters")).isEqualTo(ConsumerFilters(prisons = listOf("RolePrison")))
  }

  @Test
  fun `can get prison filters attribute from the role and the consumer`() {
    // From FiltersExtractionFilterTest
    val testRole = Role(filters = ConsumerFilters(prisons = listOf("RolePrison")))

    val resp = MockHttpServletResponse()
    val authorisationService =
      AuthorisationService(
        AuthorisationConfig(
          mapOf("consumer-name" to ConsumerConfig(include = null, filters = ConsumerFilters(prisons = listOf("MDI")), roles = listOf("test-role"))),
          roles = mapOf("test-role" to testRole),
        ),
        mockTelemetryService,
        mockManageUsersService,
      )

    val req = mockRequest("GET", examplePath)

    val chain = fullMockFilterChain(authorisationService)

    chain.doFilter(req, resp)

    assertThat(req.getAttribute("filters")).isEqualTo(ConsumerFilters(prisons = listOf("MDI", "RolePrison")))
  }

  @Test
  fun `auth filter chain test`() {
    val resp = MockHttpServletResponse()
    val finalFilter = mock(Filter::class.java)
    val authService = mockAuthService(exampleConsumer)
    val req = mockRequest("GET", examplePath)

    val chain = fullMockFilterChain(authService, finalFilter)

    chain.doFilter(req, resp)

    assertThat(req.getAttribute("clientName")).isEqualTo(exampleConsumer)
    assertThat(req.getAttribute("filters")).isEqualTo(ConsumerFilters(prisons = listOf("MDI")))
    assertThat(resp.status).isEqualTo(200)
    verify(finalFilter, times(1)).doFilter(eq(req), eq(resp), any())
  }

  // App insights request span attribute tests

  @Test
  fun `handles clientName attribute`() {
    val finalFilter = mock(Filter::class.java)
    mockFilterChain(authorisationFilter, finalFilter).doFilter(mockRequest, mockResponse)
    verify(mockTelemetryService, times(1)).setSpanAttribute("clientId", exampleConsumer)
  }

  @Test
  fun `handles missing clientName attribute`() {
    whenever(mockRequest.getHeader("subject-distinguished-name")).thenReturn(null)
    val finalFilter = mock(Filter::class.java)
    mockFilterChain(authorisationFilter, finalFilter).doFilter(mockRequest, mockResponse)
    verify(mockTelemetryService, times(0)).setSpanAttribute("clientId", exampleConsumer)
  }

  @Test
  fun `handles a certificate serial number header`() {
    whenever(certificateService.toHexFormat(any())).thenReturn(CERT_SERIAL_FORMATTED)
    val finalFilter = mock(Filter::class.java)
    mockFilterChain(authorisationFilter, finalFilter).doFilter(mockRequest, mockResponse)
    verify(mockTelemetryService, times(1)).setSpanAttribute("certSerialNumber", CERT_SERIAL_FORMATTED)
  }

  @Test
  fun `handles a NULL certificate serial number header`() {
    whenever(certificateService.getCertificateInfo(any(), any(), any())).thenReturn(
      CertificateInfo(
        null,
        exampleConsumer,
        false,
        Instant.parse("2026-05-09T00:30:10Z"),
      ),
    )
    val finalFilter = mock(Filter::class.java)
    mockFilterChain(authorisationFilter, finalFilter).doFilter(mockRequest, mockResponse)
    verify(mockTelemetryService, times(0)).setSpanAttribute("certSerialNumber", CERT_SERIAL_FORMATTED)
  }

  @Test
  fun `handles an on behalf of header when not required`() {
    whenever(mockRequest.getHeader("X-On-Behalf-Of")).thenReturn("TEST")
    val mockOboService = mock(OboService::class.java)
    whenever(mockOboService.extractUsername(any())).thenReturn("testName")
    whenever(authorisationService.oboService(any())).thenReturn(mockOboService)
    whenever(authorisationService.requiresObo(any())).thenReturn(false)
    val finalFilter = mock(Filter::class.java)
    mockFilterChain(authorisationFilter, finalFilter).doFilter(mockRequest, mockResponse)
    verify(mockTelemetryService, times(1)).setSpanAttribute("onBehalfOf", "testName")
  }

  @Test
  fun `handles an invalid on behalf of header when required`() {
    whenever(mockRequest.getHeader("X-On-Behalf-Of")).thenReturn("TEST")
    val mockOboService = mock(OboService::class.java)
    whenever(mockOboService.extractUsername(any())).thenReturn(null)
    whenever(authorisationService.oboService(any())).thenReturn(mockOboService)
    whenever(authorisationService.requiresObo(any())).thenReturn(true)
    val finalFilter = mock(Filter::class.java)
    mockFilterChain(authorisationFilter, finalFilter).doFilter(mockRequest, mockResponse)
    verify(mockResponse, times(1)).sendError(401, "On Behalf Of username unavailable for consumer-name")
  }

  @Test
  fun `handles an invalid on behalf of header that resolves to an empty string when required`() {
    whenever(mockRequest.getHeader("X-On-Behalf-Of")).thenReturn("TEST")
    val mockOboService = mock(OboService::class.java)
    whenever(mockOboService.extractUsername(any())).thenReturn("")
    whenever(authorisationService.oboService(any())).thenReturn(mockOboService)
    whenever(authorisationService.requiresObo(any())).thenReturn(true)
    val finalFilter = mock(Filter::class.java)
    mockFilterChain(authorisationFilter, finalFilter).doFilter(mockRequest, mockResponse)
    verify(mockResponse, times(1)).sendError(401, "On Behalf Of username unavailable for consumer-name")
  }

  @Test
  fun `handles an invalid on behalf of header when not required`() {
    whenever(mockRequest.getHeader("X-On-Behalf-Of")).thenReturn("TEST")
    val mockOboService = mock(OboService::class.java)
    whenever(mockOboService.extractUsername(any())).thenReturn(null)
    whenever(authorisationService.oboService(any())).thenReturn(mockOboService)
    whenever(authorisationService.requiresObo(any())).thenReturn(false)
    val finalFilter = mock(Filter::class.java)
    mockFilterChain(authorisationFilter, finalFilter).doFilter(mockRequest, mockResponse)
    verify(mockTelemetryService, times(1)).setSpanAttribute(eq("onBehalfOf"), eq("TEST"))
  }

  @Test
  fun `handles a NULL on behalf of header`() {
    whenever(mockRequest.getHeader("X-On-Behalf-Of")).thenReturn(null)
    val finalFilter = mock(Filter::class.java)
    mockFilterChain(authorisationFilter, finalFilter).doFilter(mockRequest, mockResponse)
    verify(mockTelemetryService, times(0)).setSpanAttribute(eq("onBehalfOf"), any())
  }

  @Test
  fun `returns the default consumer name when there is a default consumer name and no subject distinguished name`() {
    whenever(certificateService.getCertificateInfo(any(), any(), any())).thenReturn(
      CertificateInfo(
        CERT_SERIAL_FORMATTED,
        null,
        false,
        Instant.parse("2026-05-09T00:30:10Z"),
      ),
    )
    whenever(authorisationService.defaultConsumerName()).thenReturn(exampleConsumer)
    val finalFilter = mock(Filter::class.java)
    mockFilterChain(authorisationFilter, finalFilter).doFilter(mockRequest, mockResponse)
    verify(mockTelemetryService, times(1)).setSpanAttribute("clientId", exampleConsumer)
  }

  @Test
  fun `handles a cert-expiry-date header `() {
    whenever(certificateService.getCertificateInfo(any(), any(), any())).thenReturn(
      CertificateInfo(
        CERT_SERIAL_FORMATTED,
        exampleConsumer,
        false,
        Instant.parse("2026-05-09T00:30:10Z"),
      ),
    )
    val finalFilter = mock(Filter::class.java)
    mockFilterChain(authorisationFilter, finalFilter).doFilter(mockRequest, mockResponse)
    verify(mockTelemetryService, times(1)).setSpanAttribute("certExpiryDate", "2026-05-09T00:30:10Z")
  }

  @Test
  fun `handles a null cert-expiry-date header `() {
    whenever(certificateService.getCertificateInfo(any(), any(), any())).thenReturn(
      CertificateInfo(
        CERT_SERIAL_FORMATTED,
        exampleConsumer,
        false,
        null,
      ),
    )

    val finalFilter = mock(Filter::class.java)
    mockFilterChain(authorisationFilter, finalFilter).doFilter(mockRequest, mockResponse)
    verify(mockTelemetryService, times(0)).setSpanAttribute(eq("certExpiryDate"), any())
  }

  @Test
  fun `feature overrides allowed`() {
    val features = FeatureFlagConfig(mapOf("flag-a" to false, "flag-b" to false))
    val consumerConfig = ConsumerConfig(allowFeatureOverride = true)
    val requestConfig = authorisationFilter.featuresWithOverrides(features, consumerConfig, "flag-a=true")

    assertEquals(requestConfig.flags, mapOf("flag-a" to true, "flag-b" to false))
  }

  @Test
  fun `feature overrides denied`() {
    val features = FeatureFlagConfig(mapOf("flag-a" to false, "flag-b" to false))
    val consumerConfig = ConsumerConfig(allowFeatureOverride = false)
    val requestConfig = authorisationFilter.featuresWithOverrides(features, consumerConfig, "flag-a=true")

    assertEquals(requestConfig.flags, mapOf("flag-a" to false, "flag-b" to false))
  }
}
