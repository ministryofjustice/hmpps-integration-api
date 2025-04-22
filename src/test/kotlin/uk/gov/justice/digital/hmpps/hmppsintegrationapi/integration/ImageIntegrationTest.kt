package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ImageIntegrationTest : IntegrationTestBase() {
  @Test
  fun `returns an image from NOMIS for old endpoint`() {
    callApi("/v1/images/2461788")
      .andExpect(status().isOk)
      .andExpect(content().contentType("image/jpeg"))
  }

  private val id = 2461788
  private val path = "/v1/persons/$nomsId/images/$id"

  @Test
  fun `returns an image from NOMIS`() {
    callApi(path)
      .andExpect(status().isOk)
      .andExpect(content().contentType("image/jpeg"))
  }

  @Test
  fun `returns a 400 if the hmppsId is invalid`() {
    callApi("/v1/persons/$invalidNomsId/images/$id")
      .andExpect(status().isBadRequest)
  }

  @Test
  fun `returns a 404 if consumer is in the wrong prison`() {
    callApiWithCN(path, limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `returns a 404 if consumer has empty list of prisons`() {
    callApiWithCN(path, noPrisonsCn)
      .andExpect(status().isNotFound)
  }
}
