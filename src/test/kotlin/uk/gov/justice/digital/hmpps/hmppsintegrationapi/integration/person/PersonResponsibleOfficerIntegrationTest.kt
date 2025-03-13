package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class PersonResponsibleOfficerIntegrationTest : IntegrationTestBase() {
  @Test
  fun `returns needs for a person`() {
    callApi("$basePath/$nomsId/person-responsible-officer")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-responsible-officer")))
  }

  @Test
  fun `adjudications returns a 400 if the hmppsId is invalid`() {
    callApi("$basePath/$invalidNomsId/person-responsible-officer")
      .andExpect(status().isBadRequest)
  }

  @Test
  fun `return a 404 for person in wrong prison`() {
    callApiWithCN("$basePath/$nomsId/person-responsible-officer", limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return a 404 when no prisons in filter`() {
    callApiWithCN("$basePath/$nomsId/person-responsible-officer", noPrisonsCn)
      .andExpect(status().isNotFound)
  }
}
