package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Exclusion
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerAllocationRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerDeallocationRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.toHmppsMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.toTestMessage
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate

class ActivitiesIntegrationTest : IntegrationTestWithQueueBase("activities") {
  private val prisonCode = "MDI"

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
        "/integration-api/activities/$activityId/schedules",
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
        path = "/integration-api/schedules/$scheduleId",
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
  @DisplayName("GET /v1/activities/schedule/{scheduleId}/suitability-criteria")
  inner class GetActivityScheduleSuitabilityCriteria {
    val scheduleId = 123456L
    val path = "/v1/activities/schedule/$scheduleId/suitability-criteria"

    @Test
    fun `return the suitability criteria details`() {
      activitiesMockServer.stubForGet(
        path = "/integration-api/schedules/$scheduleId",
        File("$gatewaysFolder/activities/fixtures/GetActivityScheduleById.json").readText(),
      )
      activitiesMockServer.stubForGet(
        path = "/integration-api/activities/schedule/$scheduleId/suitability-criteria",
        File("$gatewaysFolder/activities/fixtures/GetActivitySuitabilityCriteria.json").readText(),
      )
      callApi(path)
        .andExpect(MockMvcResultMatchers.status().isOk)
        .andExpect(MockMvcResultMatchers.content().json(getExpectedResponse("activities-suitability-criteria")))
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
          incentiveLevelWarningIssued = true,
          otherAbsenceReason = "Prisoner has another reason for missing the activity",
        ),
      )

    @Test
    fun `successfully adds to queue`() {
      activitiesMockServer.stubForGet(
        "/integration-api/attendances/$attendanceId",
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
        "/integration-api/attendances/$invalidAttendanceId",
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
  }

  @Nested
  @DisplayName("GET /v1/activities/attendance-reasons")
  inner class GetAttendanceReasons {
    private val path = "/v1/activities/attendance-reasons"

    @Test
    fun `return the attendance reasons`() {
      activitiesMockServer.stubForGet(
        "/integration-api/attendance-reasons",
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
        prisonerNumber = nomsId,
        reasonCode = "RELEASED",
        endDate = LocalDate.now(),
        scheduleInstanceId = 1234L,
      )

    @Test
    fun `successfully adds to queue`() {
      activitiesMockServer.stubForGet(
        "/integration-api/schedules/$scheduleId",
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
        "/integration-api/schedules/$scheduleId",
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
        "/integration-api/schedules/$scheduleId",
        File("$gatewaysFolder/activities/fixtures/GetActivityScheduleById.json").readText().replace("\"prisonerNumber\": \"$nomsId\"", "\"prisonerNumber\": \"A1234AB\""),
      )

      val requestBody = asJsonString(prisonerDeallocationRequest)
      putApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isNotFound)

      checkQueueIsEmpty()
    }

    @Test
    fun `return a 400 if passed in end date is after the schedule end date`() {
      activitiesMockServer.stubForGet(
        "/integration-api/schedules/$scheduleId",
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
        "/integration-api/allocations/deallocation-reasons",
        File("$gatewaysFolder/activities/fixtures/GetDeallocationReasons.json").readText(),
      )
      callApi(path)
        .andExpect(MockMvcResultMatchers.status().isOk)
        .andExpect(MockMvcResultMatchers.content().json(getExpectedResponse("deallocation-reasons")))
    }
  }

  @Nested
  @DisplayName("POST /v1/activities/{scheduleId}/allocate")
  inner class PostAllocateToSchedule {
    val scheduleId = 123456L
    val path = "/v1/activities/schedule/$scheduleId/allocate"
    val prisonerAllocationRequest =
      PrisonerAllocationRequest(
        prisonerNumber = nomsId,
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

    @Test
    fun `successfully adds to queue`() {
      activitiesMockServer.stubForGet(
        "/integration-api/schedules/$scheduleId",
        File("$gatewaysFolder/activities/fixtures/GetActivityScheduleById.json")
          .readText()
          .replace("\"prisonCode\": \"PVI\"", "\"prisonCode\": \"$prisonCode\"")
          .replace("\"startDate\": \"2022-09-21\"", "\"startDate\": \"${LocalDate.now()}\"")
          .replace("\"endDate\": \"2022-10-21\"", "\"endDate\": \"${LocalDate.now().plusMonths(6)}\"")
          .replace("\"status\": \"ACTIVE\"", "\"status\": \"ENDED\""),
      )

      activitiesMockServer.stubForGet(
        "/prison/$prisonCode/prison-pay-bands",
        File("$gatewaysFolder/activities/fixtures/GetPrisonPayBands.json").readText(),
      )

      activitiesMockServer.stubForGet(
        "/integration-api/schedules/$scheduleId/waiting-list-applications",
        "[]",
      )

      val requestBody = asJsonString(prisonerAllocationRequest)
      postToApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isOk)
        .andExpect(
          MockMvcResultMatchers.content().json(
            """
            {
              "data": {
                "message": "Prisoner allocation written to queue"
              }
            }
            """.trimIndent(),
          ),
        )

      await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 1 }

      val queueMessages = getQueueMessages()
      queueMessages.size.shouldBe(1)

      val messageJson = queueMessages[0].body()
      val expectedMessage = prisonerAllocationRequest.toHmppsMessage(defaultCn, scheduleId)
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
    fun `return a 404 when prison not in the allowed prisons`() {
      val requestBody = asJsonString(prisonerAllocationRequest)
      postToApiWithCN(path, requestBody, limitedPrisonsCn)
        .andExpect(MockMvcResultMatchers.status().isNotFound)

      checkQueueIsEmpty()
    }

    @Test
    fun `return a 404 no prisons in filter`() {
      val requestBody = asJsonString(prisonerAllocationRequest)
      postToApiWithCN(path, requestBody, noPrisonsCn)
        .andExpect(MockMvcResultMatchers.status().isNotFound)

      checkQueueIsEmpty()
    }

    @Test
    fun `return 400 when allocation start date is in the past`() {
      val requestBody = asJsonString(prisonerAllocationRequest.copy(startDate = LocalDate.now().minusDays(1)))
      postToApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isBadRequest)
        .andExpect(MockMvcResultMatchers.jsonPath("$.validationErrors[0]").value("Start date must not be in the past"))
      checkQueueIsEmpty()
    }

    @Test
    fun `return 400 when allocation start date is after end date`() {
      val requestBody =
        asJsonString(
          prisonerAllocationRequest.copy(
            startDate = LocalDate.now().plusDays(10),
            endDate = LocalDate.now().plusDays(5),
          ),
        )
      postToApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isBadRequest)
        .andExpect(MockMvcResultMatchers.jsonPath("$.userMessage").value("Invalid query parameters: Allocation start date must be the same as or before the end date"))
      checkQueueIsEmpty()
    }

    @Test
    fun `return 400 when allocation is for today and scheduleInstanceId is missing`() {
      val requestBody =
        asJsonString(
          prisonerAllocationRequest.copy(
            startDate = LocalDate.now(),
          ),
        )
      postToApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isBadRequest)
        .andExpect(MockMvcResultMatchers.jsonPath("$.userMessage").value("Invalid query parameters: scheduleInstanceId must be provided when allocation start date is today"))
      checkQueueIsEmpty()
    }

    @Test
    fun `return 400 when exclusion start time is after end time`() {
      val invalidExclusion =
        Exclusion(
          timeSlot = "AM",
          weekNumber = 1,
          customStartTime = "11:00",
          customEndTime = "09:00",
          daysOfWeek = setOf(DayOfWeek.MONDAY),
          monday = false,
          tuesday = false,
          wednesday = false,
          thursday = false,
          friday = true,
          saturday = false,
          sunday = false,
        )
      val requestBody = asJsonString(prisonerAllocationRequest.copy(exclusions = listOf(invalidExclusion)))
      postToApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isBadRequest)
        .andExpect(MockMvcResultMatchers.jsonPath("$.userMessage").value("Invalid query parameters: Exclusion start time cannot be after custom end time"))
      checkQueueIsEmpty()
    }

    @Test
    fun `return 400 when paid activity and payBandId is missing`() {
      activitiesMockServer.stubForGet(
        "/integration-api/schedules/$scheduleId",
        File("$gatewaysFolder/activities/fixtures/GetActivityScheduleById.json")
          .readText()
          .replace("\"paid\": false", "\"paid\": true"),
      )
      val requestBody = asJsonString(prisonerAllocationRequest.copy(payBandId = null))
      postToApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isBadRequest)
        .andExpect(MockMvcResultMatchers.jsonPath("$.userMessage").value("Invalid query parameters: Allocation must have a pay band when the activity is paid"))
      checkQueueIsEmpty()
    }

    @Test
    fun `return 400 when unpaid activity and payBandId is present`() {
      activitiesMockServer.stubForGet(
        "/integration-api/schedules/$scheduleId",
        File("$gatewaysFolder/activities/fixtures/GetActivityScheduleById.json")
          .readText()
          .replace("\"paid\": true", "\"paid\": false"),
      )
      val requestBody = asJsonString(prisonerAllocationRequest)
      postToApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isBadRequest)
        .andExpect(MockMvcResultMatchers.jsonPath("$.userMessage").value("Invalid query parameters: Allocation cannot have a pay band when the activity is unpaid"))
      checkQueueIsEmpty()
    }

    @Test
    fun `return 400 when pay band does not exist for the prison`() {
      activitiesMockServer.stubForGet(
        "/integration-api/schedules/$scheduleId",
        File("$gatewaysFolder/activities/fixtures/GetActivityScheduleById.json").readText().replace("\"prisonCode\": \"PVI\"", "\"prisonCode\": \"$prisonCode\""),
      )
      activitiesMockServer.stubForGet(
        "/prison/$prisonCode/prison-pay-bands",
        "[]",
      )
      val requestBody = asJsonString(prisonerAllocationRequest)
      postToApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isBadRequest)
        .andExpect(MockMvcResultMatchers.jsonPath("$.userMessage").value("Invalid query parameters: Pay band '123456' does not exist for prison '$prisonCode'"))
      checkQueueIsEmpty()
    }

    @Test
    fun `return 400 when allocation start date is before schedule start date`() {
      val scheduleStart = LocalDate.now().plusDays(5)
      activitiesMockServer.stubForGet(
        "/integration-api/schedules/$scheduleId",
        File("$gatewaysFolder/activities/fixtures/GetActivityScheduleById.json")
          .readText()
          .replace("\"prisonCode\": \"PVI\"", "\"prisonCode\": \"$prisonCode\"")
          .replace("\"startDate\": \"2022-09-21\"", "\"startDate\": \"${LocalDate.now().plusDays(5)}\""),
      )

      activitiesMockServer.stubForGet(
        "/prison/$prisonCode/prison-pay-bands",
        File("$gatewaysFolder/activities/fixtures/GetPrisonPayBands.json").readText(),
      )
      val requestBody = asJsonString(prisonerAllocationRequest.copy(startDate = LocalDate.now().plusDays(1)))
      postToApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isBadRequest)
        .andExpect(MockMvcResultMatchers.jsonPath("$.userMessage").value("Invalid query parameters: Allocation start date must not be before the activity schedule start date ($scheduleStart)"))
      checkQueueIsEmpty()
    }

    @Test
    fun `return 400 when allocation end date is after schedule end date`() {
      val scheduleEnd = LocalDate.now().plusDays(10)
      activitiesMockServer.stubForGet(
        "/integration-api/schedules/$scheduleId",
        File("$gatewaysFolder/activities/fixtures/GetActivityScheduleById.json")
          .readText()
          .replace("\"prisonCode\": \"PVI\"", "\"prisonCode\": \"$prisonCode\"")
          .replace("\"endDate\": \"2022-10-21\"", "\"endDate\": \"${scheduleEnd}\""),
      )

      activitiesMockServer.stubForGet(
        "/prison/$prisonCode/prison-pay-bands",
        File("$gatewaysFolder/activities/fixtures/GetPrisonPayBands.json").readText(),
      )

      val requestBody =
        asJsonString(
          prisonerAllocationRequest.copy(
            startDate = LocalDate.now().plusDays(5),
            endDate = LocalDate.now().plusDays(20),
          ),
        )
      postToApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isBadRequest)
        .andExpect(MockMvcResultMatchers.jsonPath("$.userMessage").value("Invalid query parameters: Allocation end date must not be after the activity schedule end date ($scheduleEnd)"))
      checkQueueIsEmpty()
    }

    @Test
    fun `return 400 when allocation start date is after schedule end date`() {
      val scheduleEnd = LocalDate.now().plusDays(5)
      activitiesMockServer.stubForGet(
        "/integration-api/schedules/$scheduleId",
        File("$gatewaysFolder/activities/fixtures/GetActivityScheduleById.json")
          .readText()
          .replace("\"prisonCode\": \"PVI\"", "\"prisonCode\": \"$prisonCode\"")
          .replace("\"endDate\": \"2022-10-21\"", "\"endDate\": \"${scheduleEnd}\""),
      )

      activitiesMockServer.stubForGet(
        "/prison/$prisonCode/prison-pay-bands",
        File("$gatewaysFolder/activities/fixtures/GetPrisonPayBands.json").readText(),
      )

      val requestBody = asJsonString(prisonerAllocationRequest.copy(startDate = LocalDate.now().plusDays(10), endDate = null))
      postToApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isBadRequest)
        .andExpect(MockMvcResultMatchers.jsonPath("$.userMessage").value("Invalid query parameters: Allocation start date cannot be after the activity schedule end date ($scheduleEnd)"))
      checkQueueIsEmpty()
    }

    @Test
    fun `return 400 when prisoner is already allocated to the schedule`() {
      activitiesMockServer.stubForGet(
        "/integration-api/schedules/$scheduleId",
        File("$gatewaysFolder/activities/fixtures/GetActivityScheduleById.json")
          .readText()
          .replace("\"prisonCode\": \"PVI\"", "\"prisonCode\": \"$prisonCode\"")
          .replace("\"startDate\": \"2022-09-21\"", "\"startDate\": \"${LocalDate.now().plusDays(4)}\"")
          .replace("\"endDate\": \"2022-10-21\"", "\"endDate\": \"${LocalDate.now().plusMonths(6)}\"")
          .replace("\"status\": \"ENDED\"", "\"status\": \"ACTIVE\""),
      )

      activitiesMockServer.stubForGet(
        "/prison/$prisonCode/prison-pay-bands",
        File("$gatewaysFolder/activities/fixtures/GetPrisonPayBands.json").readText(),
      )

      val requestBody = asJsonString(prisonerAllocationRequest.copy(startDate = LocalDate.now().plusDays(5), endDate = null))
      postToApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isBadRequest)
        .andExpect(MockMvcResultMatchers.jsonPath("$.userMessage").value("Invalid query parameters: Prisoner with $nomsId is already allocated to schedule Entry level Maths 1."))

      checkQueueIsEmpty()
    }

    @Test
    fun `return 400 when exclusion slot does not exist in schedule`() {
      activitiesMockServer.stubForGet(
        "/integration-api/schedules/$scheduleId",
        File("$gatewaysFolder/activities/fixtures/GetActivityScheduleById.json")
          .readText()
          .replace("\"prisonCode\": \"PVI\"", "\"prisonCode\": \"$prisonCode\"")
          .replace("\"startDate\": \"2022-09-21\"", "\"startDate\": \"${LocalDate.now().plusDays(4)}\"")
          .replace("\"endDate\": \"2022-10-21\"", "\"endDate\": \"${LocalDate.now().plusMonths(6)}\"")
          .replace("\"status\": \"ACTIVE\"", "\"status\": \"ENDED\""),
      )

      activitiesMockServer.stubForGet(
        "/prison/$prisonCode/prison-pay-bands",
        File("$gatewaysFolder/activities/fixtures/GetPrisonPayBands.json").readText(),
      )

      val requestBody =
        asJsonString(
          prisonerAllocationRequest.copy(
            startDate = LocalDate.now().plusDays(5),
            endDate = null,
            exclusions =
              prisonerAllocationRequest.exclusions
                ?.mapIndexed { i, exclusion -> if (i == 0) exclusion.copy(monday = false, tuesday = false, wednesday = false, friday = true) else exclusion },
          ),
        )

      postToApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isBadRequest)
        .andExpect(MockMvcResultMatchers.jsonPath("$.userMessage").value("Invalid query parameters: No AM slots in week number 1 for schedule 123456"))
      checkQueueIsEmpty()
    }

    @Test
    fun `return 409 when prisoner has a PENDING waiting list application`() {
      activitiesMockServer.stubForGet(
        "/integration-api/schedules/$scheduleId",
        File("$gatewaysFolder/activities/fixtures/GetActivityScheduleById.json")
          .readText()
          .replace("\"prisonCode\": \"PVI\"", "\"prisonCode\": \"$prisonCode\"")
          .replace("\"startDate\": \"2022-09-21\"", "\"startDate\": \"${LocalDate.now().plusDays(4)}\"")
          .replace("\"endDate\": \"2022-10-21\"", "\"endDate\": \"${LocalDate.now().plusMonths(6)}\"")
          .replace("\"status\": \"ACTIVE\"", "\"status\": \"ENDED\""),
      )

      activitiesMockServer.stubForGet(
        "/prison/$prisonCode/prison-pay-bands",
        File("$gatewaysFolder/activities/fixtures/GetPrisonPayBands.json").readText(),
      )

      activitiesMockServer.stubForGet(
        "/integration-api/schedules/$scheduleId/waiting-list-applications",
        File("$gatewaysFolder/activities/fixtures/GetWaitingListApplicationsByScheduleId.json").readText(),
      )

      val requestBody =
        asJsonString(
          prisonerAllocationRequest.copy(
            startDate = LocalDate.now().plusDays(5),
            endDate = null,
          ),
        )

      postToApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isConflict)
        .andExpect(MockMvcResultMatchers.jsonPath("$.userMessage").value("Conflict: Prisoner has a PENDING waiting list application. It must be APPROVED before they can be allocated."))
      checkQueueIsEmpty()
    }

    @Test
    fun `return 409 when prisoner has more than one APPROVED waiting list application`() {
      activitiesMockServer.stubForGet(
        "/integration-api/schedules/$scheduleId",
        File("$gatewaysFolder/activities/fixtures/GetActivityScheduleById.json")
          .readText()
          .replace("\"prisonCode\": \"PVI\"", "\"prisonCode\": \"$prisonCode\"")
          .replace("\"startDate\": \"2022-09-21\"", "\"startDate\": \"${LocalDate.now().plusDays(4)}\"")
          .replace("\"endDate\": \"2022-10-21\"", "\"endDate\": \"${LocalDate.now().plusMonths(6)}\"")
          .replace("\"status\": \"ACTIVE\"", "\"status\": \"ENDED\""),
      )

      activitiesMockServer.stubForGet(
        "/prison/$prisonCode/prison-pay-bands",
        File("$gatewaysFolder/activities/fixtures/GetPrisonPayBands.json").readText(),
      )

      activitiesMockServer.stubForGet(
        "/integration-api/schedules/$scheduleId/waiting-list-applications",
        File("$gatewaysFolder/activities/fixtures/GetWaitingListApplicationsByScheduleIdMultipleApproved.json").readText(),
      )

      val requestBody =
        asJsonString(
          prisonerAllocationRequest.copy(
            startDate = LocalDate.now().plusDays(5),
            endDate = null,
          ),
        )

      postToApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isConflict)
        .andExpect(MockMvcResultMatchers.jsonPath("$.userMessage").value("Conflict: Prisoner has more than one APPROVED waiting list application. A prisoner can only have one approved waiting list application."))
      checkQueueIsEmpty()
    }

    @Test
    fun `successfully adds test message to queue`() {
      val requestBody = asJsonString(prisonerAllocationRequest.copy(testEvent = "TestEvent"))
      postToApi(path, requestBody)
        .andExpect(MockMvcResultMatchers.status().isOk)
        .andExpect(
          MockMvcResultMatchers.content().json(
            """
            {
              "data": {
                "message": "Prisoner allocation written to queue"
              }
            }
            """.trimIndent(),
          ),
        )

      await untilCallTo { getNumberOfMessagesCurrentlyOnQueue() } matches { it == 1 }

      val queueMessages = getQueueMessages()
      queueMessages.size.shouldBe(1)

      val messageJson = queueMessages[0].body()
      val expectedMessage = prisonerAllocationRequest.toTestMessage(defaultCn)
      messageJson.shouldContainJsonKeyValue("$.eventType", expectedMessage.eventType.eventTypeCode)
      messageJson.shouldContainJsonKeyValue("$.who", defaultCn)
      val objectMapper = jacksonObjectMapper()
      val messageAttributes = objectMapper.readTree(messageJson).at("/messageAttributes")
      val expectedMessageAttributes = objectMapper.readTree(objectMapper.writeValueAsString(expectedMessage.messageAttributes))
      messageAttributes.shouldBe(expectedMessageAttributes)
    }
  }

  @Nested
  @DisplayName("GET /v1/activities/schedule/{scheduleId}/waiting-list-applications")
  inner class GetWaitingListApplicationsByScheduleId {
    private val scheduleId = 111111L
    private val path = "/v1/activities/schedule/$scheduleId/waiting-list-applications"
    private val badRequestPath = "/v1//activities/schedule/AAA/waiting-list-applications"

    @Test
    fun `return waiting list applications`() {
      activitiesMockServer.stubForGet(
        "/integration-api/schedules/$scheduleId",
        File("$gatewaysFolder/activities/fixtures/GetActivityScheduleById.json").readText(),
      )

      activitiesMockServer.stubForGet(
        "/integration-api/schedules/$scheduleId/waiting-list-applications",
        File("$gatewaysFolder/activities/fixtures/GetWaitingListApplicationsByScheduleId.json").readText(),
      )

      callApi(path)
        .andExpect(MockMvcResultMatchers.status().isOk)
        .andExpect(MockMvcResultMatchers.content().json(getExpectedResponse("waiting-list-applications-response")))

      activitiesMockServer.verify(
        getRequestedFor(urlEqualTo("/integration-api/schedules/$scheduleId/waiting-list-applications"))
          .withHeader("Caseload-Id", equalTo(prisonCode)),
      )
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
    fun `return a 400 Bad Request when a string is provided as the schedule ID`() {
      callApi(badRequestPath)
        .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }
  }
}
