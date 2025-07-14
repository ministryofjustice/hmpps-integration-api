package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test

class StatusIntegrationTest : IntegrationTestBase() {
  @Test
  fun `Status test`() {
    callApi("/v1/status")
      .andExpect(status().isOk)
      .andExpect(
        content().json(
          """
        {"status":"ok"}
        """,
        ),
      )
  }
}
