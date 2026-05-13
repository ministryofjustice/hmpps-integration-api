package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.AuthorisationFilter
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.MappaCategory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.RoleService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import kotlin.test.Test

@AutoConfigureMockMvc
class FiltersIntegrationTest : IntegrationTestBase() {
  val filtersCapture = ArgumentCaptor.forClass(ConsumerFilters::class.java)
  val mockRequest = mock(HttpServletRequest::class.java)
  val mockResponse = mock(HttpServletResponse::class.java)
  val mockChain = mock(FilterChain::class.java)
  val mockTelemetryService = mock(TelemetryService::class.java)

  lateinit var filtersExtractionFilter: AuthorisationFilter

  @BeforeEach
  fun setup() {
    whenever(mockRequest.requestURI).thenReturn("/v1/persons/$crn")
    filtersExtractionFilter =
      AuthorisationFilter(
        authorisationService,
        mockTelemetryService,
        RoleService(),
      )
    whenever(mockRequest.getHeader("cert-serial-number")).thenReturn(certSerialNumber)
  }

  @Test
  fun `if a wildcard is found in any mappa configration then set categories to null`() {
    // Given the following config
    //    automated-test-client-mappa:
    //      roles:
    //        - "full-access"
    //        - "mappa-cat4"
    //      filters:
    //        mappa-categories:
    //          - "*"
    whenever(mockRequest.getHeader("subject-distinguished-name")).thenReturn("C=GB,ST=London,L=London,O=Home Office,CN=automated-test-client-mappa")
    filtersExtractionFilter.doFilter(mockRequest, mockResponse, mockChain)
    verify(mockRequest, times(1)).setAttribute(eq("filters"), filtersCapture.capture())
    val mappaCategories = filtersCapture.value?.mappaCategories
    assertThat(mappaCategories).isNull()
  }

  @Test
  fun `collates all mappa categories from config into consumer filters`() {
    // Given the following config
    //    roles:
    //      - "full-access"
    //      - "mappa-cat4"
    //    filters:
    //      mappa-categories:
    //        - CAT1
    whenever(mockRequest.getHeader("subject-distinguished-name")).thenReturn("C=GB,ST=London,L=London,O=Home Office,CN=automated-test-client-mappa-2")
    filtersExtractionFilter.doFilter(mockRequest, mockResponse, mockChain)
    verify(mockRequest, times(1)).setAttribute(eq("filters"), filtersCapture.capture())
    val mappaCategories = filtersCapture.value.mappaCategories
    assertThat(mappaCategories).isEqualTo(listOf(MappaCategory.CAT1, MappaCategory.CAT4))
  }
}
