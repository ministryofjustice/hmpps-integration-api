package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AddCaseNoteRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AttendanceUpdateRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerDeallocationRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.toHmppsMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.toTestMessage
import java.io.File
import java.time.LocalDate

class ActivitiesIntegrationTest : IntegrationTestWithQueueBase("activities") {
  @Nested
  @DisplayName("GET /v1/activities/{activityId}/schedules")
  inner class GetActivitySchedules {
    private val activityId = 123456L
    private val stringActivityId = "AAAAAAAA"
    private val path = "/v1/activities/$activityId/schedules"
    private val badRequestPath = "/v1/activities/$stringActivityId/schedules"

    @Test
    fun `return the activity's schedule`() {
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
  @DisplayName("GET /v1/activities/schedule/{scheduleId}")
  inner class GetScheduleDetails {
    val scheduleId = 123456L
    val path = "/v1/activities/schedule/$scheduleId"

    @Test
    fun `return the activity schedule details`() {
      activitiesMockServer.stubForGet(
        path = "/schedules/$scheduleId",
        File("$gatewaysFolder/activities/fixtures/GetActivityScheduleById.json").readText(),
      )
      callApi(path)
        .andExpect(MockMvcResultMatchers.status().isOk)
        .andExpect(MockMvcResultMatchers.content().json(getExpectedResponse("activities-schedule-detailed-response")))
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
        "/attendance-reasons",
        File("$gatewaysFolder/activities/fixtures/GetAttendanceReasons.json").readText(),
      )
      callApi(path)
        .andExpect(MockMvcResultMatchers.status().isOk)
        .andExpect(MockMvcResultMatchers.content().json(getExpectedResponse("reasons-for-attendance")))
    }
  }

  @Nested
  @DisplayName("PUT /v1/activities/{scheduleId}/deallocate")
  inner class PutDeallocateFromSchedule {
    val scheduleId = 123456L
    val path = "/v1/activities/schedule/$scheduleId/deallocate"
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

    @Test
    fun `successfully adds to queue`() {
      activitiesMockServer.stubForGet(
        "/schedules/$scheduleId",
        File("$gatewaysFolder/activities/fixtures/GetActivityScheduleById.json").readText().replace("\"endDate\": \"2022-10-21\"", "\"endDate\": \"${LocalDate.now()}\""),
      )

      val requestBody = asJsonString(prisonerDeallocationRequest)
      putApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isOk)
        .andExpect(
          MockMvcResultMatchers.content().json(
            """
            {
              "data": {
                "message": "Prisoner deallocation written to queue"
              }
            }
            """.trimIndent(),
          ),
        )

      await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 1 }

      val queueMessages = getQueueMessages()
      queueMessages.size.shouldBe(1)

      val messageJson = queueMessages[0].body()
      val expectedMessage = prisonerDeallocationRequest.toHmppsMessage(defaultCn, scheduleId)
      messageJson.shouldContainJsonKeyValue("$.eventType", expectedMessage.eventType.eventTypeCode)
      messageJson.shouldContainJsonKeyValue("$.who", defaultCn)
      val objectMapper = jacksonObjectMapper()
      val messageAttributes = objectMapper.readTree(messageJson).at("/messageAttributes")
      val expectedMessageAttributes =
        objectMapper.readTree(
          objectMapper
            .registerModule(JavaTimeModule())
            .disable(
              SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
            ).writeValueAsString(expectedMessage.messageAttributes),
        )
      messageAttributes.shouldBe(expectedMessageAttributes)
    }

    @Test
    fun `successfully adds test message to queue`() {
      val requestBody = asJsonString(prisonerDeallocationRequest.copy(reasonCode = "TestEvent"))
      putApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isOk)
        .andExpect(
          MockMvcResultMatchers.content().json(
            """
            {
              "data": {
                "message": "Prisoner deallocation written to queue"
              }
            }
            """.trimIndent(),
          ),
        )

      await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 1 }

      val queueMessages = getQueueMessages()
      queueMessages.size.shouldBe(1)

      val messageJson = queueMessages[0].body()
      val expectedMessage = prisonerDeallocationRequest.toTestMessage(defaultCn)
      messageJson.shouldContainJsonKeyValue("$.eventType", expectedMessage.eventType.eventTypeCode)
      messageJson.shouldContainJsonKeyValue("$.who", defaultCn)
      val objectMapper = jacksonObjectMapper()
      val messageAttributes = objectMapper.readTree(messageJson).at("/messageAttributes")
      val expectedMessageAttributes = objectMapper.readTree(objectMapper.writeValueAsString(expectedMessage.messageAttributes))
      messageAttributes.shouldBe(expectedMessageAttributes)
    }

    @Test
    fun `return a 404 when prison not in the allowed prisons`() {
      val requestBody = asJsonString(prisonerDeallocationRequest)
      putApiWithCN(path, requestBody, limitedPrisonsCn)
        .andExpect(MockMvcResultMatchers.status().isNotFound)

      checkQueueIsEmpty()
    }

    @Test
    fun `return a 404 no prisons in filter`() {
      val requestBody = asJsonString(prisonerDeallocationRequest)
      putApiWithCN(path, requestBody, noPrisonsCn)
        .andExpect(MockMvcResultMatchers.status().isNotFound)

      checkQueueIsEmpty()
    }

    @Test
    fun `return a 404 when schedule record does not exist`() {
      activitiesMockServer.stubForGet(
        "/schedules/$scheduleId",
        "",
        HttpStatus.NOT_FOUND,
      )

      val requestBody = asJsonString(prisonerDeallocationRequest)
      putApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isNotFound)

      checkQueueIsEmpty()
    }

    @Test
    fun `return a 404 no allocations are found for the prisoner`() {
      activitiesMockServer.stubForGet(
        "/schedules/$scheduleId",
        File("$gatewaysFolder/activities/fixtures/GetActivityScheduleById.json").readText().replace("\"prisonerNumber\": \"A1234AA\"", "\"prisonerNumber\": \"A1234AB\""),
      )

      val requestBody = asJsonString(prisonerDeallocationRequest)
      putApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isNotFound)

      checkQueueIsEmpty()
    }

    @Test
    fun `return a 400 if passed in end date is after the schedule end date`() {
      activitiesMockServer.stubForGet(
        "/schedules/$scheduleId",
        File("$gatewaysFolder/activities/fixtures/GetActivityScheduleById.json").readText(),
      )

      val requestBody = asJsonString(prisonerDeallocationRequest)
      putApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isBadRequest)

      checkQueueIsEmpty()
    }

    @Test
    fun `return a 400 when prisonerNumber is blank`() {
      val requestBody = asJsonString(prisonerDeallocationRequest.copy(prisonerNumber = ""))
      putApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isBadRequest)

      checkQueueIsEmpty()
    }

    @Test
    fun `return a 400 when reasonCode is blank`() {
      val requestBody = asJsonString(prisonerDeallocationRequest.copy(reasonCode = ""))
      putApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isBadRequest)

      checkQueueIsEmpty()
    }

    @Test
    fun `return a 400 when endDate is in the past`() {
      val requestBody = asJsonString(prisonerDeallocationRequest.copy(endDate = LocalDate.now().minusDays(1)))
      putApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isBadRequest)

      checkQueueIsEmpty()
    }
  }

  @Nested
  @DisplayName("GET /v1/activities/deallocation-reasons")
  inner class GetDeallocationReasons {
    private val path = "/v1/activities/deallocation-reasons"

    @Test
    fun `return the deallocation reasons`() {
      activitiesMockServer.stubForGet(
        "/allocations/deallocation-reasons",
        File("$gatewaysFolder/activities/fixtures/GetDeallocationReasons.json").readText(),
      )
      callApi(path)
        .andExpect(MockMvcResultMatchers.status().isOk)
        .andExpect(MockMvcResultMatchers.content().json(getExpectedResponse("deallocation-reasons")))
    }
  }
}
