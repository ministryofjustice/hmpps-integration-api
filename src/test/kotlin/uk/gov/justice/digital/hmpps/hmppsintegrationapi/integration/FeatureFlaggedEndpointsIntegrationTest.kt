package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig

internal class FeatureFlaggedEndpointsIntegrationTest : IntegrationTestBase() {
  @MockitoBean
  private lateinit var featureFlagConfig: FeatureFlagConfig

  @Test
  fun `physical characteristics endpoint should return 503`() {
    whenever(featureFlagConfig.usePhysicalCharacteristicsEndpoints).thenReturn(false)
    callApi("$basePath/$nomsId/physical-characteristics")
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `images by id endpoint should return 503`() {
    whenever(featureFlagConfig.useImageEndpoints).thenReturn(false)
    callApi("$basePath/$nomsId/images/2461788")
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `residential summary should return 503`() {
    whenever(featureFlagConfig.useResidentialHierarchyEndpoints).thenReturn(false)
    val prisonId = "MDI"
    val path = "/v1/prison/$prisonId/residential-hierarchy"
    callApi(path)
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `residential details should return 503`() {
    whenever(featureFlagConfig.useLocationEndpoint).thenReturn(false)
    val prisonId = "MDI"
    val locationId = "A1-B1-C1"
    val path = "/v1/prison/$prisonId/location/$locationId"
    callApi(path)
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `residential details should return 503`() {
    whenever(featureFlagConfig.useResidentialDetailsEndpoints).thenReturn(false)
    val prisonId = "MDI"
    val path = "/v1/prison/$prisonId/residential-details?parentPathHierarchy=A"
    callApi(path)
      .andExpect(status().isServiceUnavailable)
  }
}
