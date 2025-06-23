package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_ATTENDANCE_REASONS_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_LANGUAGES_ENDPOINTS
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_LOCATION_DEACTIVATE_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_LOCATION_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_PERSONAL_CARE_NEEDS_ENDPOINTS
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_PRISON_ACTIVITIES_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_PRISON_PAY_BANDS_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_PRISON_REGIME_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_RESIDENTIAL_DETAILS_ENDPOINTS
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_RESIDENTIAL_HIERARCHY_ENDPOINTS
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_SCHEDULE_DETAIL_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_UPDATE_ATTENDANCE_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.FeatureNotEnabledException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DeactivateLocationRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DeactivationReason
import java.time.LocalDate

internal class FeatureFlaggedEndpointsIntegrationTest : IntegrationTestBase() {
  @MockitoBean
  private lateinit var featureFlagConfig: FeatureFlagConfig

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
    val key = "MDI-A-1-001"
    val path = "/v1/prison/$prisonId/location/$key/deactivate"
    val deactivateLocationRequest =
      DeactivateLocationRequest(
        deactivationReason = DeactivationReason.DAMAGED,
        deactivationReasonDescription = "Scheduled maintenance",
        proposedReactivationDate = LocalDate.now(),
      )
    postToApi(path, asJsonString(deactivateLocationRequest))
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `care needs should return 503`() {
    whenever(featureFlagConfig.require(USE_PERSONAL_CARE_NEEDS_ENDPOINTS)).thenThrow(FeatureNotEnabledException(""))
    val path = "$basePath/$nomsId/care-needs"
    callApi(path)
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `languages should return 503`() {
    whenever(featureFlagConfig.require(USE_LANGUAGES_ENDPOINTS)).thenThrow(FeatureNotEnabledException(""))
    val path = "$basePath/$nomsId/languages"
    callApi(path)
      .andExpect(status().isServiceUnavailable)
  }

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
