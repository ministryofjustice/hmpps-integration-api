package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class EPFPersonDetailIntegrationTest : IntegrationTestBase() {
  @Test
  fun `returns a person detail for a probation case, by HmppsID`() {
    callApi("/v1/epf/person-details/$crn/1234")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-probation-information")))
  }
}
