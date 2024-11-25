package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class PrisonIntegrationTest() : IntegrationTestBase() {

  @Test
  fun `returns nomis number for a person given hmpps id`() {
    callApi("/v0/prison/$hmppsId")
      .andExpect(status().isOk)
  }
}
