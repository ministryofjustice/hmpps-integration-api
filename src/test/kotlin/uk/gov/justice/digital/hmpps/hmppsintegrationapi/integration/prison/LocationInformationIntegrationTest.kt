package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.prison

import org.junit.jupiter.api.Test
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class LocationInformationIntegrationTest : IntegrationTestBase() {
  private final val baseLocationInformationPath = "/v1/prison/$prisonId/location/$cellKey"

  @Test
  fun `return a 200 when successful upstream response`() {
    callApi(baseLocationInformationPath)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("location-information-response.json"), JsonCompareMode.STRICT))
  }

  @Test
  fun `return a 404 when consumer does not have access to provided prisonId`() {
    callApiWithCN(baseLocationInformationPath, noPrisonsCn)
      .andExpect(status().isNotFound)
  }
}
