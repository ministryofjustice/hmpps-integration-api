package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.prison

import org.junit.jupiter.api.Test
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class PssDetailIntegrationTest : IntegrationTestBase() {
  final val path = "/v1/prison/prisoners/{hmppsId}/pss-detail"

  @Test
  fun `return the prisoner pss detail for a crn`() {
    callApi("/v1/prison/prisoners/$crn/pss-detail")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("prison-pss-details-response.json"), JsonCompareMode.STRICT))
  }

  @Test
  fun `return the prisoner pss detail for a nomis id`() {
    callApi("/v1/prison/prisoners/$nomsId/pss-detail")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("prison-pss-details-response.json"), JsonCompareMode.STRICT))
  }

  @Test
  fun `return a 400 with an INVALID hmpps Id`() {
    callApi("/v1/prison/prisoners/INVALID/pss-detail")
      .andExpect(status().isBadRequest)
  }

  @Test
  fun `return a 404 with an unknown hmpps Id (crn)`() {
    callApi("/v1/prison/prisoners/Y358756/pss-detail")
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return a 404 with an unknown hmpps Id (nomis id)`() {
    callApi("/v1/prison/prisoners/Z1234JK/pss-detail")
      .andExpect(status().isNotFound)
  }
}
