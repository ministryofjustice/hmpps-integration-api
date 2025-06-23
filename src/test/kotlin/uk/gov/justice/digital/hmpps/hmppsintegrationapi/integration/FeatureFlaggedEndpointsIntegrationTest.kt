package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_SCHEDULE_DETAIL_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_UPDATE_ATTENDANCE_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_DEALLOCATION_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.FeatureNotEnabledException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AddCaseNoteRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DeactivateLocationRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DeactivationReason
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerDeallocationRequest
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
  fun `put deallocate should return 503`() {
    whenever(featureFlagConfig.require(USE_DEALLOCATION_ENDPOINT)).thenThrow(FeatureNotEnabledException(""))
    val path = "/v1/activities/schedule/1234/deallocate"
    val prisonerDeallocationRequest =
      PrisonerDeallocationRequest(
        prisonerNumber = "A1234AA",
        reasonCode = "RELEASED",
        endDate = LocalDate.now(),
        caseNote =
          AddCaseNoteRequest(
            type = "GEN",
            text = "Case note text",
          ),
        scheduleInstanceId = 1234L,
      )

    putApi(path, asJsonString(prisonerDeallocationRequest))
      .andExpect(status().isServiceUnavailable)
  }
}
