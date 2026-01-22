package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

@TestPropertySource(properties = ["services.assess-risks-and-needs.base-url=http://localhost:4032"])
class RiskPredictorScoresIntegrationTest : IntegrationTestBase() {
  val deliusCrn = "A123456"

  @MockitoSpyBean
  private lateinit var featureFlagConfig: FeatureFlagConfig

  @BeforeEach
  fun setUp() {
    arnsMockServer.start()
  }

  @AfterEach
  fun tearDown() {
    arnsMockServer.stop()
    arnsMockServer.resetValidator()
  }

  @Test
  fun `risk scores endpoint returns OK when new endpoint is NOT enabled and uses deprecated arns endpoint`() {
    whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.USE_NEW_RISK_SCORE_API)).thenReturn(false)
    arnsMockServer.stubForGet(
      "/risks/predictors/$deliusCrn",
      getExpectedResponse("arns-risk-predictor-scores-deprecated.json"),
    )

    callApi("$basePath/$deliusCrn/risks/scores")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-risk-scores.json")))
    arnsMockServer.assertValidationPassed()
  }

  @Test
  fun `risk scores endpoint returns OK when new endpoint is enabled and uses new endpoint with version 1`() {
    whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.USE_NEW_RISK_SCORE_API)).thenReturn(true)
    arnsMockServer.stubForGet(
      "/risks/predictors/unsafe/all/CRN/$deliusCrn",
      getExpectedResponse("arns-risk-predictor-scores-new-v1-only.json"),
    )

    callApi("$basePath/$deliusCrn/risks/scores")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-risk-scores-v1.json")))
    arnsMockServer.assertValidationPassed()
  }

  @Test
  fun `risk scores endpoint returns OK when new endpoint is enabled and uses new endpoint with version 2`() {
    whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.USE_NEW_RISK_SCORE_API)).thenReturn(true)
    arnsMockServer.stubForGet(
      "/risks/predictors/unsafe/all/CRN/$deliusCrn",
      getExpectedResponse("arns-risk-predictor-scores-new-v2-only.json"),
    )

    callApi("$basePath/$deliusCrn/risks/scores")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-risk-scores-v2.json")))
    arnsMockServer.assertValidationPassed()
  }

  @Test
  fun `risk scores endpoint returns OK when new endpoint is enabled and uses new endpoint with version 1 and version 2`() {
    whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.USE_NEW_RISK_SCORE_API)).thenReturn(true)
    arnsMockServer.stubForGet(
      "/risks/predictors/unsafe/all/CRN/$deliusCrn",
      getExpectedResponse("arns-risk-predictor-scores-new-v1-and-v2.json"),
    )

    callApi("$basePath/$deliusCrn/risks/scores")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-risk-scores-v1-and-v2.json")))
    arnsMockServer.assertValidationPassed()
  }
}
