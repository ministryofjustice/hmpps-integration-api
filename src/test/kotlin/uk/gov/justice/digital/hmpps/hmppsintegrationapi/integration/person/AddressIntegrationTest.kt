package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class AddressIntegrationTest : IntegrationTestBase() {
  @Test
  fun `returns addresses for a person`() {
    callApi("$basePath/$encodedHmppsId/addresses")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-addresses")))
  }
}
