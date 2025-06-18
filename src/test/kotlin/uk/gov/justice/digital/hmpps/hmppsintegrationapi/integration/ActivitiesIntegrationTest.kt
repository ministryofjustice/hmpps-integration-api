package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.io.File

class ActivitiesIntegrationTest : IntegrationTestBase() {
  private val activityId = 123456L
  private val path = "/v1/activities/$activityId/schedules"

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
