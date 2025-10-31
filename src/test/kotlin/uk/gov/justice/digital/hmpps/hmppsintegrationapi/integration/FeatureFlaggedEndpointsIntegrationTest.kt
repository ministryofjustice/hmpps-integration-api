package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_ALLOCATION_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_DEALLOCATION_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_DEALLOCATION_REASONS_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_EDUCATION_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_ESWE_CURIOUS_ENDPOINTS
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_HISTORICAL_ATTENDANCES_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_PRISONER_BASE_LOCATION_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_SCHEDULED_INSTANCES_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_SCHEDULE_DETAIL_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_SEARCH_APPOINTMENTS_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_SUITABILITY_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_UPDATE_ATTENDANCE_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_WAITING_LIST_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.FeatureNotEnabledException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Exclusion
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerAllocationRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerDeallocationRequest
import java.time.DayOfWeek
import java.time.LocalDate

internal class FeatureFlaggedEndpointsIntegrationTest : IntegrationTestBase() {
  @MockitoBean
  private lateinit var featureFlagConfig: FeatureFlagConfig

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
  fun `get schedule details should return 503`() {
    whenever(featureFlagConfig.require(USE_SCHEDULE_DETAIL_ENDPOINT)).thenThrow(FeatureNotEnabledException(""))
    val scheduleId = 1234L
    val path = "/v1/activities/schedule/$scheduleId"
    callApi(path)
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `get activity schedule suitability criteria should return 503`() {
    whenever(featureFlagConfig.require(USE_SUITABILITY_ENDPOINT)).thenThrow(FeatureNotEnabledException(""))
    val scheduleId = 1234L
    val path = "/v1/activities/schedule/$scheduleId/suitability-criteria"
    callApi(path)
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `get education for person should return 503`() {
    whenever(featureFlagConfig.require(USE_EDUCATION_ENDPOINT)).thenThrow(FeatureNotEnabledException(""))
    val hmppsId = "A1234AA"
    val path = "/v1/persons/$hmppsId/education"
    callApi(path)
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `post search appointments should return 503`() {
    val prisonId = "MDI"
    whenever(featureFlagConfig.require(USE_SEARCH_APPOINTMENTS_ENDPOINT)).thenThrow(FeatureNotEnabledException(""))
    val path = "/v1/prison/$prisonId/appointments/search"
    val requestBody =
      """
      {
        "appointmentType": "INDIVIDUAL",
        "startDate": "2025-06-16",
        "endDate": "2025-06-16",
        "timeSlots": [
          "AM",
          "PM",
          "ED"
        ],
        "categoryCode": "GYMW",
        "inCell": false,
        "prisonerNumbers": [
          "A1234BC"
        ]
      }
      """.trimIndent()
    postToApi(path, requestBody)
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `put deallocate should return 503`() {
    whenever(featureFlagConfig.require(USE_DEALLOCATION_ENDPOINT)).thenThrow(FeatureNotEnabledException(""))
    val path = "/v1/activities/schedule/1234/deallocate"
    val prisonerDeallocationRequest =
      PrisonerDeallocationRequest(
        prisonerNumber = "A1234AA",
        reasonCode = "RELEASED",
        endDate = LocalDate.now(),
        scheduleInstanceId = 1234L,
      )

    putApi(path, asJsonString(prisonerDeallocationRequest))
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `get scheduled instances for prisoner should return 503`() {
    whenever(featureFlagConfig.require(USE_SCHEDULED_INSTANCES_ENDPOINT)).thenThrow(FeatureNotEnabledException(""))
    val prisonId = "MDI"
    val hmppsId = "A1234AA"
    val path = "/v1/prison/$prisonId/prisoners/$hmppsId/scheduled-instances?startDate=2022-09-10&endDate=2023-09-10"
    callApi(path)
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `get deallocation reasons should return 503`() {
    whenever(featureFlagConfig.require(USE_DEALLOCATION_REASONS_ENDPOINT)).thenThrow(FeatureNotEnabledException(""))
    val path = "/v1/activities/deallocation-reasons"
    callApi(path)
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `post allocate should return 503`() {
    whenever(featureFlagConfig.require(USE_ALLOCATION_ENDPOINT)).thenThrow(FeatureNotEnabledException(""))
    val scheduleId = 123456L
    val prisonerNumber = "A1234AA"
    val path = "/v1/activities/schedule/$scheduleId/allocate"
    val prisonerAllocationRequest =
      PrisonerAllocationRequest(
        prisonerNumber = prisonerNumber,
        startDate = LocalDate.now().plusMonths(1),
        endDate = LocalDate.now().plusMonths(2),
        payBandId = 123456L,
        exclusions =
          listOf(
            Exclusion(
              timeSlot = "AM",
              weekNumber = 1,
              customStartTime = "09:00",
              customEndTime = "11:00",
              daysOfWeek = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY),
              monday = true,
              tuesday = true,
              wednesday = true,
              thursday = false,
              friday = false,
              saturday = false,
              sunday = false,
            ),
          ),
      )

    postToApi(path, asJsonString(prisonerAllocationRequest))
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `get education assessment should return 503`() {
    whenever(featureFlagConfig.require(USE_ESWE_CURIOUS_ENDPOINTS)).thenThrow(FeatureNotEnabledException(""))
    val prisonNumber = "A1234AA"
    val path = "/v1/persons/$prisonNumber/education/assessments"

    callApi(path)
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `get prisoner base location should return 503`() {
    whenever(featureFlagConfig.require(USE_PRISONER_BASE_LOCATION_ENDPOINT)).thenThrow(FeatureNotEnabledException(""))
    val prisonNumber = "A1234AA"
    val path = "/v1/persons/$prisonNumber/prisoner-base-location"

    callApi(path)
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `get historical attendances should return 503`() {
    whenever(featureFlagConfig.require(USE_HISTORICAL_ATTENDANCES_ENDPOINT)).thenThrow(FeatureNotEnabledException(""))
    val startDate = "2022-01-01"
    val endDate = "2022-02-01"
    val prisonId = "MDI"
    val path = "/v1/prison/prisoners/$nomsId/activities/attendances?startDate=$startDate&endDate=$endDate&prisonId=$prisonId"
    callApi(path)
      .andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `get waiting list applications by schedule ID should return 503`() {
    whenever(featureFlagConfig.require(USE_WAITING_LIST_ENDPOINT)).thenThrow(FeatureNotEnabledException(""))
    val scheduleId = 123456L
    val filters = null
    val path = "/v1/activities/schedule/$scheduleId/waiting-list-applications"
    callApi(path)
      .andExpect(status().isServiceUnavailable)
  }
}
