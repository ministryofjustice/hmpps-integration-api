package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import java.io.File
import kotlin.test.Test

@TestPropertySource(properties = ["services.assess-risks-and-needs.base-url=http://localhost:4032"])
class AssessmentSummaryIntegrationTest : IntegrationTestBase() {
  val testAssessmentSummary =
    """
          {
            "data": {
              "initiationDate": "2026-01-16T16:22:54",
              "completedDate": "2026-02-05T09:13:21",
              "assessmentType": "Test Assessment Type",
              "status": "Test Assessment Status",
              "assessorName": "Test Assessor Name",
              "countersignerName": "Test Countersigner Name"
            }
          }
        """.removeWhitespaceAndNewlines()

  val stubbedAssessmentSummary =
    """
          {
            "data": {
              "initiationDate": "2026-01-05T11:28:32",
              "completedDate": "2026-02-22T15:09:03",
              "assessmentType": "Stubbed Assessment Type",
              "status": "Stubbed Assessment Status",
              "assessorName": "Stubbed Assessor Name",
              "countersignerName": "Stubbed Countersigner Name"
            }
          }
        """.removeWhitespaceAndNewlines()

  @BeforeEach
  fun setUp() {
    arnsMockServer.start()
    arnsMockServer.stubForGet(
      "/assessment-summary/$crn",
      File(
        "$gatewaysFolder/assessrisksandneeds/fixtures/AssessmentSummaryResponse.json",
      ).readText(),
    )
  }

  @AfterEach
  fun tearDown() {
    arnsMockServer.stop()
  }

  @Test
  fun `can retrieve an assessment summary for a crn`() {
    callApi("$basePath/$crn/assessment-summary")
      .andExpect(status().isOk)
      .andExpect(content().json(testAssessmentSummary))
  }

  @Test
  fun `can retrieve a stubbed assessment summary for a crn`() {
    whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.USE_STUBBED_ASSESSMENT_SUMMARY)).thenReturn(true)
    callApi("$basePath/$crn/assessment-summary")
      .andExpect(status().isOk)
      .andExpect(content().json(stubbedAssessmentSummary))
  }

  @Test
  fun `can retrieve an assessment summary for a nomis number`() {
    callApi("$basePath/$nomsId/assessment-summary")
      .andExpect(status().isOk)
      .andExpect(content().json(testAssessmentSummary))
  }

  @Test
  fun `can retrieve a stubbed assessment summary for a nomis number`() {
    whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.USE_STUBBED_ASSESSMENT_SUMMARY)).thenReturn(true)
    callApi("$basePath/$nomsId/assessment-summary")
      .andExpect(status().isOk)
      .andExpect(content().json(stubbedAssessmentSummary))
  }

  @Test
  fun `returns a 404`() {
    arnsMockServer.stubForGet(
      "/assessment-summary/$crn",
      "",
      HttpStatus.NOT_FOUND,
    )
    callApi("$basePath/$crn/assessment-summary")
      .andExpect(status().isNotFound)
  }

  @Test
  fun `cannot access endpoint when feature disabled`() {
    whenever(featureFlagConfig.getConfigFlagValue(FeatureFlagConfig.USE_ASSESSMENT_SUMMARY_ENDPOINT)).thenReturn(false)
    callApi("$basePath/$crn/assessment-summary")
      .andExpect(status().isServiceUnavailable)
  }
}
