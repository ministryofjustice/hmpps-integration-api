package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway

class CacheIntegrationTest : IntegrationTestBase() {
  private final val hmppsId = "G2996UX"
  private final val path = "/v1/persons/$hmppsId/addresses"

  @MockitoBean
  private lateinit var featureFlagConfig: FeatureFlagConfig

  @MockitoSpyBean
  private lateinit var prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway

  @BeforeEach
  fun setUp() {
    whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.CPR_ENABLED)).thenReturn(false)
  }

  @Test
  fun `caches prisoner data when getPrisonOffender called twice`() {
    callApiWithCN(path, specificPrisonCn)
      .andExpect(status().isOk)
    // Calls the cached method only once
    verify(prisonerOffenderSearchGateway, times(1)).getPrisonOffender(hmppsId)
  }
}
