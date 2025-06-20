package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.prison

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import java.io.File

class PrisonActivitiesIntegrationTest : IntegrationTestBase() {
  private val prisonId = "MDI"

  @Nested
  @DisplayName("GET prison-activities")
  inner class GetPrisonActivities {
    private val path = "/v1/prison/$prisonId/activities"

    @Test
    fun `return the prison activities details`() {
      activitiesMockServer.stubForGet(
        "/prison/$prisonId/activities",
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
        "/appointments/$prisonId/search",
        reqBody = requestBody,
        File("$gatewaysFolder/activities/fixtures/GetAppointments.json").readText(),
      )
      postToApi(path, requestBody)
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("search-appointments-response")))
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
}
