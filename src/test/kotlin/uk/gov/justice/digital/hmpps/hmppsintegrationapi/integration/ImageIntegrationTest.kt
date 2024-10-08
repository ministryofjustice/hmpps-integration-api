package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ImageIntegrationTest : IntegrationTestBase() {
  @Test
  fun `returns an image from NOMIS`() {
    callApi("/v1/images/2461788")
      .andExpect(status().isOk)
      .andExpect(content().contentType("image/jpeg"))
  }
}
