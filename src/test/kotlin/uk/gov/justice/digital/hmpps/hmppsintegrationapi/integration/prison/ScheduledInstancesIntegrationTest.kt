package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.prison

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import java.io.File

class ScheduledInstancesIntegrationTest : IntegrationTestBase() {
  private val prisonId = "MDI"
  private val path = "/v1/prison/$prisonId/prisoners/$nomsId/scheduled-instances?startDate=2022-09-10&endDate=2023-09-10"

  @AfterEach
  fun resetValidators() {
    activitiesMockServer.resetValidator()
  }

  @Test
  fun `return the scheduled instances`() {
    activitiesMockServer.stubForGet(
      "/integration-api/prisons/$prisonId/$nomsId/scheduled-instances?startDate=2022-09-10&endDate=2023-09-10",
      File("$gatewaysFolder/activities/fixtures/GetActivitiesScheduledInstanceForPrisoner.json").readText(),
    )
    callApi(path)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("scheduled-instances-response")))

    activitiesMockServer.assertValidationPassed()
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
