package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.prison

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class LocationInformationIntegrationTest : IntegrationTestBase() {
  private final val prisonId = "MDI"
  private final val locationId = "MDI-A1-B1-C1"
  private final val baseLocationInformationPath = "/v1/prison/$prisonId/location/$locationId"

  @Test
  fun `return a 200 when successful upstream response`() {
    callApi(baseLocationInformationPath)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("location-information-response")))
  }

  @Test
  fun `return a 404 when consumer does not have access to provided prisonId`() {
    callApiWithCN(baseLocationInformationPath, noPrisonsCn)
      .andExpect(status().isNotFound)
  }
}
