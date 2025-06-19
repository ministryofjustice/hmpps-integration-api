package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.matchers.shouldBe
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AttendanceUpdateRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.toHmppsMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.toTestMessage
import java.io.File

class ActivitiesIntegrationTest : IntegrationTestWithQueueBase("activities") {
  @Nested
  @DisplayName("GET /v1/activities/{activityId}/schedules")
  inner class GetActivitySchedules {
    private val activityId = 123456L
    private val stringActivityId = "AAAAAAAA"
    private val path = "/v1/activities/$activityId/schedules"
    private val badRequestPath = "/v1/activities/$stringActivityId/schedules"

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

    @Test
    fun `return a 400 Bad Request when a string is provided as the activity ID`() {
      callApi(badRequestPath)
        .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }
  }

  @Nested
  @DisplayName("PUT /v1/activities/schedule/attendance")
  inner class PutAttendance {
    val path = "/v1/activities/schedule/attendance"
    val attendanceId = 123456L
    val attendanceUpdateRequests =
      listOf(
        AttendanceUpdateRequest(
          id = attendanceId,
          prisonId = "MDI",
          status = "WAITING",
          attendanceReason = "ATTENDED",
          comment = "Prisoner has COVID-19",
          issuePayment = true,
          caseNote = "Prisoner refused to attend the scheduled activity without reasonable excuse",
          incentiveLevelWarningIssued = true,
          otherAbsenceReason = "Prisoner has another reason for missing the activity",
        ),
      )

    @Test
    fun `successfully adds to queue`() {
      activitiesMockServer.stubForGet(
        "/attendances/$attendanceId",
        File("$gatewaysFolder/activities/fixtures/GetAttendanceById.json").readText(),
      )

      val requestBody = asJsonString(attendanceUpdateRequests)
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

      await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 1 }

      val queueMessages = getQueueMessages()
      queueMessages.size.shouldBe(1)

      val messageJson = queueMessages[0].body()
      val expectedMessage = attendanceUpdateRequests.toHmppsMessage(defaultCn)
      messageJson.shouldContainJsonKeyValue("$.eventType", expectedMessage.eventType.eventTypeCode)
      messageJson.shouldContainJsonKeyValue("$.who", defaultCn)
      val objectMapper = jacksonObjectMapper()
      val messageAttributes = objectMapper.readTree(messageJson).at("/messageAttributes")
      val expectedMessageAttributes = objectMapper.readTree(objectMapper.writeValueAsString(expectedMessage.messageAttributes))
      messageAttributes.shouldBe(expectedMessageAttributes)
    }

    @Test
    fun `successfully adds test message to queue`() {
      val requestBody = asJsonString(attendanceUpdateRequests.map { it.copy(status = "TestEvent") })
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

      await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 1 }

      val queueMessages = getQueueMessages()
      queueMessages.size.shouldBe(1)

      val messageJson = queueMessages[0].body()
      val expectedMessage = attendanceUpdateRequests.toTestMessage(defaultCn)
      messageJson.shouldContainJsonKeyValue("$.eventType", expectedMessage.eventType.eventTypeCode)
      messageJson.shouldContainJsonKeyValue("$.who", defaultCn)
      val objectMapper = jacksonObjectMapper()
      val messageAttributes = objectMapper.readTree(messageJson).at("/messageAttributes")
      val expectedMessageAttributes = objectMapper.readTree(objectMapper.writeValueAsString(expectedMessage.messageAttributes))
      messageAttributes.shouldBe(expectedMessageAttributes)
    }

    @Test
    fun `return a 404 when prison not in the allowed prisons`() {
      val requestBody = asJsonString(attendanceUpdateRequests)
      putApiWithCN(path, requestBody, limitedPrisonsCn)
        .andExpect(MockMvcResultMatchers.status().isNotFound)

      checkQueueIsEmpty()
    }

    @Test
    fun `return a 404 no prisons in filter`() {
      val requestBody = asJsonString(attendanceUpdateRequests)
      putApiWithCN(path, requestBody, noPrisonsCn)
        .andExpect(MockMvcResultMatchers.status().isNotFound)

      checkQueueIsEmpty()
    }

    @Test
    fun `return a 404 when attendance record does not exist`() {
      val invalidAttendanceId = 234567L
      activitiesMockServer.stubForGet(
        "/attendances/$invalidAttendanceId",
        "",
        HttpStatus.NOT_FOUND,
      )

      val requestBody = asJsonString(attendanceUpdateRequests.map { it.copy(id = invalidAttendanceId) })
      putApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isNotFound)

      checkQueueIsEmpty()
    }

    @Test
    fun `return a 400 when attendance id is blank`() {
      val requestBody = asJsonString(attendanceUpdateRequests.map { it.copy(id = 0) })
      putApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isBadRequest)

      checkQueueIsEmpty()
    }

    @Test
    fun `return a 400 when prison id is blank`() {
      val requestBody = asJsonString(attendanceUpdateRequests.map { it.copy(prisonId = "") })
      putApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isBadRequest)

      checkQueueIsEmpty()
    }

    @Test
    fun `return a 400 when status is blank`() {
      val requestBody = asJsonString(attendanceUpdateRequests.map { it.copy(status = "") })
      putApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isBadRequest)

      checkQueueIsEmpty()
    }

    @Test
    fun `return a 400 when case notes exceeds max length`() {
      val requestBody = asJsonString(attendanceUpdateRequests.map { it.copy(caseNote = "a".repeat(3801)) })
      putApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isBadRequest)

      checkQueueIsEmpty()
    }
  }

  @Nested
  @DisplayName("GET /v1/activities/attendance-reasons")
  inner class GetAttendanceReasons {
    private val path = "/v1/activities/attendance-reasons"

    @Test
    fun `return the attendance reasons`() {
      activitiesMockServer.stubForGet(
        "/activities/attendance-reasons",
        File("$gatewaysFolder/activities/fixtures/GetReasonsForAttendance.json").readText(),
      )
      callApi(path)
        .andExpect(MockMvcResultMatchers.status().isOk)
        .andExpect(MockMvcResultMatchers.content().json(getExpectedResponse("reasons-for-attendance")))
    }

    @Test
    fun `return a 403 for a forbidden request`() {
      callApi(path)
        .andExpect(MockMvcResultMatchers.status().isForbidden)
    }
  }
}
