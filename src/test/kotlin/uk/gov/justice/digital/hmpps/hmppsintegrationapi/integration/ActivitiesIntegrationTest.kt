package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.io.File

class ActivitiesIntegrationTest : IntegrationTestBase() {
  @Nested
  @DisplayName("GET /v1/activities/{activityId}/schedules")
  inner class GetActivitySchedules {
    val activityId = 123456L
    val path = "/v1/activities/$activityId/schedules"

    @Test
    fun `return the activity schedule details`() {
      activitiesMockServer.stubForGet(
        "/activities/$activityId/schedules",
        File("$gatewaysFolder/activities/fixtures/GetActivitiesSchedule.json").readText(),
      )
      callApi(path)
        .andExpect(MockMvcResultMatchers.status().isOk)
        .andExpect(MockMvcResultMatchers.content().json(getExpectedResponse("activities-schedule-response")))
    }

    @Test
    fun `return a 404 when prison not in the allowed prisons`() {
      callApiWithCN(path, limitedPrisonsCn)
        .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `return a 404 no prisons in filter`() {
      callApiWithCN(path, noPrisonsCn)
        .andExpect(MockMvcResultMatchers.status().isNotFound)
    }
  }

  @Nested
  @DisplayName("PUT /v1/activities/schedule/attendance")
  inner class PutAttendance {
    val path = "/v1/activities/schedule/attendance"
    val attendanceId = 123456L
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

    @Test
    fun `successfully adds to queue`() {
      activitiesMockServer.stubForGet(
        "/attendances/$attendanceId",
        File("$gatewaysFolder/activities/fixtures/GetAttendanceById.json").readText(),
      )

      putApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isOk)
        .andExpect(
          MockMvcResultMatchers.content().json(
            """
            {
              "data": {
                "message": "Attendance update written to queue"
              }
            }
            """.trimIndent(),
          ),
        )
    }

    @Test
    fun `return a 404 when prison not in the allowed prisons`() {
      putApiWithCN(path, requestBody, limitedPrisonsCn)
        .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `return a 404 no prisons in filter`() {
      putApiWithCN(path, requestBody, noPrisonsCn)
        .andExpect(MockMvcResultMatchers.status().isNotFound)
    }
  }
}
