package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class TierIntegrationTest : IntegrationTestBase() {
  val crnPath = "$basePath/$crn/tier/details"
  val nomisPath = "$basePath/$nomsId/tier/details"
  val invalidPath = "$basePath/1234AA/tier/details"
  val notfoundPath = "$basePath/A987564/tier/details"

  @Test
  fun `returns tier details for a person using a CRN`() {
    callApi(crnPath)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("tier-details-response.json"), JsonCompareMode.STRICT))
  }

  @Test
  fun `returns tier details for a person using a NOMIS ID`() {
    callApi(nomisPath)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("tier-details-response.json"), JsonCompareMode.STRICT))
  }

  @Test
  fun `upstream tier endpoint returns a not found`() {
    callApi(notfoundPath)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `hmpps id is not valid`() {
    callApi(invalidPath)
      .andExpect(status().isBadRequest)
  }
}
