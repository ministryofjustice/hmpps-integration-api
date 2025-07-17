package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.prison

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import java.io.File

class PrisonPayBandsIntegrationTest : IntegrationTestBase() {
  private val prisonId = "MDI"
  private val path = "/v1/prison/$prisonId/prison-pay-bands"

  @AfterEach
  fun resetValidators() {
    activitiesMockServer.resetValidator()
  }

  @Test
  fun `return the prison pay bands`() {
    activitiesMockServer.stubForGet(
      "/integration-api/prison/$prisonId/prison-pay-bands",
      File("$gatewaysFolder/activities/fixtures/GetPrisonPayBands.json").readText(),
    )
    callApi(path)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("prison-pay-bands-response")))

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
