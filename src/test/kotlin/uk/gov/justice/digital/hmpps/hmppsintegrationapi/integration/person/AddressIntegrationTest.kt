package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class AddressIntegrationTest : IntegrationTestBase() {
  val path = "$basePath/$nomsId/addresses"

  @Test
  fun `returns addresses for a person`() {
    callApi(path)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-addresses")))
  }

  @Test
  fun `sentences returns a 400 if the hmppsId is invalid`() {
    callApi("$basePath/$invalidNomsId/addresses")
      .andExpect(status().isBadRequest)
  }

  @Test
  fun `sentences returns a 404 for if consumer has empty list of prisons`() {
    callApiWithCN(path, noPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `sentences returns a 404 for prisoner in wrong prison`() {
    callApiWithCN(path, limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }
}
