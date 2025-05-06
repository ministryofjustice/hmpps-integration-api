package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_IMAGE_ENDPOINTS
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_LOCATION_DEACTIVATE_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_LOCATION_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_PHYSICAL_CHARACTERISTICS_ENDPOINTS
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_RESIDENTIAL_DETAILS_ENDPOINTS
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_RESIDENTIAL_HIERARCHY_ENDPOINTS
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.FeatureNotEnabledException

internal class FeatureFlaggedEndpointsIntegrationTest : IntegrationTestBase() {
  @MockitoBean
  private lateinit var featureFlagConfig: FeatureFlagConfig

  @Test
  fun `physical characteristics endpoint should return 503`() {
    whenever(featureFlagConfig.require(USE_PHYSICAL_CHARACTERISTICS_ENDPOINTS)).thenThrow(FeatureNotEnabledException(""))
    callApi("$basePath/$nomsId/physical-characteristics")
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `images by id endpoint should return 503`() {
    whenever(featureFlagConfig.require(USE_IMAGE_ENDPOINTS)).thenThrow(FeatureNotEnabledException(""))
    callApi("$basePath/$nomsId/images/2461788")
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `residential summary should return 503`() {
    whenever(featureFlagConfig.require(USE_RESIDENTIAL_HIERARCHY_ENDPOINTS)).thenThrow(FeatureNotEnabledException(""))
    val prisonId = "MDI"
    val path = "/v1/prison/$prisonId/residential-hierarchy"
    callApi(path)
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `location details should return 503`() {
    whenever(featureFlagConfig.isEnabled(USE_LOCATION_ENDPOINT)).thenReturn(false)

    val prisonId = "MDI"
    val locationId = "MDI-A1-B1-C1"
    val path = "/v1/prison/$prisonId/location/$locationId"
    callApi(path)
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `residential details should return 503`() {
    whenever(featureFlagConfig.require(USE_RESIDENTIAL_DETAILS_ENDPOINTS)).thenThrow(FeatureNotEnabledException(""))
    val prisonId = "MDI"
    val path = "/v1/prison/$prisonId/residential-details?parentPathHierarchy=A"
    callApi(path)
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `location deactivate should return 503`() {
    whenever(featureFlagConfig.require(USE_LOCATION_DEACTIVATE_ENDPOINT)).thenThrow(FeatureNotEnabledException(""))
    val prisonId = "MDI"
    val path = "/v1/prison/$prisonId/residential-details?parentPathHierarchy=A"
    callApi(path)
      .andExpect(status().isServiceUnavailable)
  }
}
