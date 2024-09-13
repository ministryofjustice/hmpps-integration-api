package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class PersonResponsibleOfficerIntegrationTest : IntegrationTestBase() {

  @Test
  fun `returns needs for a person`() {
    callApi("$basePath/$pnc/person-responsible-officer")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-responsible-officer")))
  }

}