package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_ATTENDANCE_REASONS_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_PRISON_ACTIVITIES_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_PRISON_PAY_BANDS_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_PRISON_REGIME_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_SCHEDULE_DETAIL_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_UPDATE_ATTENDANCE_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.FeatureNotEnabledException

internal class FeatureFlaggedEndpointsIntegrationTest : IntegrationTestBase() {
  @MockitoBean
  private lateinit var featureFlagConfig: FeatureFlagConfig

  @Test
  fun `prison regime should return 503`() {
    whenever(featureFlagConfig.require(USE_PRISON_REGIME_ENDPOINT)).thenThrow(FeatureNotEnabledException(""))
    val prisonId = "MDI"
    val path = "/v1/prison/$prisonId/prison-regime"
    callApi(path)
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `prison activities should return 503`() {
    whenever(featureFlagConfig.require(USE_PRISON_ACTIVITIES_ENDPOINT)).thenThrow(FeatureNotEnabledException(""))
    val prisonId = "MDI"
    val path = "/v1/prison/$prisonId/activities"
    callApi(path)
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `activities schedule should return 503`() {
    whenever(featureFlagConfig.require(USE_PRISON_ACTIVITIES_ENDPOINT)).thenThrow(FeatureNotEnabledException(""))
    val activityId = 123456L
    val path = "/v1/activities/$activityId/schedules"
    callApi(path)
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `prison pay bands should return 503`() {
    whenever(featureFlagConfig.require(USE_PRISON_PAY_BANDS_ENDPOINT)).thenThrow(FeatureNotEnabledException(""))
    val prisonId = "MDI"
    val path = "/v1/prison/$prisonId/prison-pay-bands"
    callApi(path)
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `put attendance should return 503`() {
    whenever(featureFlagConfig.require(USE_UPDATE_ATTENDANCE_ENDPOINT)).thenThrow(FeatureNotEnabledException(""))
    val path = "/v1/activities/schedule/attendance"
    val requestBody =
      """
      [
        {
          "id": 123456,
          "prisonId": "MDI",
          "status": "WAITING",
          "attendanceReason": "ATTENDED",
          "comment": "Prisoner has COVID-19",
          "issuePayment": true,
          "caseNote": "Prisoner refused to attend the scheduled activity without reasonable excuse",
          "incentiveLevelWarningIssued": true,
          "otherAbsenceReason": "Prisoner has another reason for missing the activity"
        }
      ]
      """.trimIndent()
    putApi(path, requestBody)
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `attendance reasons should return 503`() {
    whenever(featureFlagConfig.require(USE_ATTENDANCE_REASONS_ENDPOINT)).thenThrow(FeatureNotEnabledException(""))
    val path = "/v1/activities/attendance-reasons"
    callApi(path)
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `get schedule details should return 503`() {
    whenever(featureFlagConfig.require(USE_SCHEDULE_DETAIL_ENDPOINT)).thenThrow(FeatureNotEnabledException(""))
    val scheduleId = 1234L
    val path = "/v1/activities/schedule/$scheduleId"
    callApi(path)
      .andExpect(status().isServiceUnavailable)
  }
}
