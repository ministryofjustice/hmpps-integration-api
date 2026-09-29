package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.prison

import org.junit.jupiter.api.Test
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class LiveRollIntegrationTest : IntegrationTestBase() {
  final val path = "/v1/prison/$prisonId/live-roll"

  @Test
  fun `return a list prisoners with all fields populated`() {
    callApi(path)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("live-roll.json"), JsonCompareMode.STRICT))
  }
}
