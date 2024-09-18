package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class HmppsIdIntegrationTest : IntegrationTestBase() {
  @Test
  fun `gets the person detail`() {
    callApi("/v1/hmpps/id/nomis-number/$nomsId")
      .andExpect(status().isOk)
      .andExpect(
        content().json(
          """
        {"data":{"hmppsId":"A123456"}}
      """,
        ),
      )
  }
}
