package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.prison

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import java.io.File

class ScheduledInstancesIntegrationTest : IntegrationTestBase() {
  private val prisonCode = "MDI"
  private val prisonerId = "A1234AA"
  private val path = "/v1/prison/$prisonCode/$prisonerId/scheduled-instances?startDate=2022-09-10&endDate=2023-09-10"

  @Test
  fun `return the scheduled instances`() {
    activitiesMockServer.stubForGet(
      "/prisons/$prisonCode/$prisonerId/scheduled-instances?startDate=2022-09-10&endDate=2023-09-10",
      File("$gatewaysFolder/activities/fixtures/GetActivitiesScheduledInstanceForPrisoner.json").readText(),
    )
    callApi(path)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("scheduled-instances-response")))
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
