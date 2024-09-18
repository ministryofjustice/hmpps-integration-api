package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class LicenceConditionIntegrationTest : IntegrationTestBase() {
  @Test
  fun `returns alerts for a person`() {
    callApi("$basePath/$crn/licences/conditions")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-licence-conditions")))
  }
}
