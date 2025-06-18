package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.prison

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import java.io.File

class ActivitiesScheduleIntegrationTest : IntegrationTestBase() {
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
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("activities-schedule-response")))
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

  @Test
  fun `return a 400 Bad Request when a string is provided as the activity ID`() {
    callApi(badRequestPath)
      .andExpect(status().isBadRequest)
  }
}
