package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.assertj.core.api.Assertions.assertThat
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.FiltersExtractionFilter
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
class FiltersIntegrationTest {
  @Autowired lateinit var authorisationConfig: AuthorisationConfig

  @Test
  fun `ignore mappa categories with a wildcard and include all of the mappa categories after the filter`() {
    // Given the following config

    //    automated-test-client-mappa:
    //      roles:
    //        - "full-access"
    //        - "mappa-cat4"
    //      filters:
    //        mappa-categories:
    //          - "*"

    val filtersExtractionFilter =
      FiltersExtractionFilter(
        authorisationConfig,
      )
    val filtersCapture = ArgumentCaptor.forClass(ConsumerFilters::class.java)
    val mockRequest = mock(HttpServletRequest::class.java)
    val mockResponse = mock(HttpServletResponse::class.java)
    val mockChain = mock(FilterChain::class.java)
    whenever(mockRequest.getAttribute("clientName")).thenReturn("automated-test-client-mappa")
    filtersExtractionFilter.doFilter(mockRequest, mockResponse, mockChain)
    verify(mockRequest, times(1)).setAttribute(eq("filters"), filtersCapture.capture())
    val mappaCategories = filtersCapture.value.mappaCategories
    assertThat(mappaCategories?.size).isEqualTo(1)
  }
}
