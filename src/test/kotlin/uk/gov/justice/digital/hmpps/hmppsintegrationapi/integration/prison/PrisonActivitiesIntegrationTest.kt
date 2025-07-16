package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.prison

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AppointmentSearchRequest
import java.io.File
import java.time.LocalDate

class PrisonActivitiesIntegrationTest : IntegrationTestBase() {
  private val prisonId = "MDI"

  @Nested
  @DisplayName("GET prison-activities")
  inner class GetPrisonActivities {
    private val path = "/v1/prison/$prisonId/activities"

    @Test
    fun `return the prison activities details`() {
      activitiesMockServer.stubForGet(
        "/integration-api/prison/$prisonId/activities",
        File("$gatewaysFolder/activities/fixtures/GetAllRunningActivities.json").readText(),
      )
      callApi(path)
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("prison-activities-response")))
    }

    @Test
    fun `return a 404 when prison not in the allowed prisons`() {
      callApiWithCN(path, limitedPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 404 no prisons in filter`() {
      callApiWithCN(path, noPrisonsCn)
        .andExpect(status().isNotFound)
    }
  }

  @Nested
  @DisplayName("POST search appointments")
  inner class SearchAppointments {
    private val path = "/v1/prison/$prisonId/appointments/search"
    val appointmentSearchRequest =
      AppointmentSearchRequest(
        appointmentType = "INDIVIDUAL",
        startDate = LocalDate.parse("2025-06-16"),
        endDate = LocalDate.parse("2025-06-16"),
        timeSlots = listOf("AM", "PM", "ED"),
        categoryCode = "GYMW",
        inCell = false,
        prisonerNumbers = listOf("A1234BC"),
      )
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

    @Test
    fun `return the appointments details`() {
      activitiesMockServer.stubForPost(
        "/integration-api/appointments/$prisonId/search",
        reqBody = requestBody,
        File("$gatewaysFolder/activities/fixtures/GetAppointments.json").readText(),
      )
      postToApi(path, requestBody)
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("search-appointments-response")))
    }

    @Test
    fun `return a 400 when request body is blank, at least startDate should be provided`() {
      val requestBody = "{}"
      postToApi(path, requestBody)
        .andExpect(status().isBadRequest)
    }

    @Test
    fun `return a 404 when prison not in the allowed prisons`() {
      postToApiWithCN(path, requestBody, limitedPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 404 no prisons in filter`() {
      postToApiWithCN(path, requestBody, noPrisonsCn)
        .andExpect(status().isNotFound)
    }
  }

  @Nested
  @DisplayName("GET /v1/prison/prisoners/{hmppsId}/activities/attendances ")
  inner class GetHistoricalAttendances {
    private val startDate = "2022-01-01"
    private val endDate = "2022-02-01"
    val path = "/v1/prison/prisoners/$nomsId/activities/attendances?startDate=$startDate&endDate=$endDate&prisonId=$prisonId"

    @Test
    fun `return the historical attendances`() {
      activitiesMockServer.stubForGet(
        "/integration-api/attendances/prisoner/$nomsId?startDate=$startDate&endDate=$endDate&prisonCode=$prisonId",
        File("$gatewaysFolder/activities/fixtures/GetHistoricalAttendances.json").readText(),
      )
      callApi(path)
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("historical-attendances-response")))
    }

    @Test
    fun `return a 404 when prison not in the allowed prisons`() {
      callApiWithCN(path, limitedPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 404 no prisons in filter`() {
      callApiWithCN(path, noPrisonsCn)
        .andExpect(status().isNotFound)
    }
  }
}
