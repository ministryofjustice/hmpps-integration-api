package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.prison

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class PrisonIntegrationTest : IntegrationTestBase() {
  private final val hmppsId = "G2996UX"
  private final val basePrisonPath = "/v1/prison"

  @Test
  fun `return a prisoner with all fields populated`() {
    callApi("$basePrisonPath/prisoners/$hmppsId")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("prisoner-response")))
  }
}
