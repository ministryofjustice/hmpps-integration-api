package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class AlertsWithApiQueryFeatureIntegrationTest : IntegrationTestBase() {
  @MockitoBean lateinit var featureFlagConfig: FeatureFlagConfig

  @BeforeEach
  fun setUp() {
    whenever(featureFlagConfig.getConfigFlagValue(FeatureFlagConfig.NORMALISED_PATH_MATCHING)).thenReturn(true)
  }

  @Nested
  inner class GetAlerts {
    val path = "$basePath/$nomsId/alerts"

    @Test
    fun `returns alerts for a person`() {
      callApi(path)
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("person-alerts")))
    }

    @Test
    fun `returns a 400 if the hmppsId is invalid`() {
      callApi("$basePath/$invalidNomsId/alerts")
        .andExpect(status().isBadRequest)
    }

    @Test
    fun `return a 404 for person in wrong prison`() {
      callApiWithCN(path, limitedPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 404 when no prisons in filter`() {
      callApiWithCN(path, noPrisonsCn)
        .andExpect(status().isNotFound)
    }
  }

  @Nested
  inner class GetPndAlerts {
    val path = "/v1/pnd/persons/$nomsId/alerts"

    @Test
    fun `returns unfiltered PND alerts for a person`() {
      callApi(path)
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("person-alerts")))
    }

    @Test
    fun `returns a 400 if the hmppsId is invalid`() {
      callApi("/v1/pnd/persons/$invalidNomsId/alerts")
        .andExpect(status().isBadRequest)
    }

    @Test
    fun `return a 404 for person in wrong prison`() {
      callApiWithCN(path, limitedPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 404 when no prisons in filter`() {
      callApiWithCN(path, noPrisonsCn)
        .andExpect(status().isNotFound)
    }
  }
}
