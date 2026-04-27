package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class RiskManagementIntegrationTest : IntegrationTestBase() {
  @Test
  fun `returns protected characteristics for a person`() {
    callApi("$basePath/$crn/risk-management-plan")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-risk-plan")))
  }

  @Test
  fun `returns protected characteristics for a person for a nomis number`() {
    callApi("$basePath/$nomsId/risk-management-plan")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-risk-plan")))
  }

  @Test
  fun `returns not found when a crn cannot be resolved for the supplied nomis number`() {
    callApi("$basePath/A1234AB/risk-management-plan")
      .andExpect(status().isNotFound)
  }
}
