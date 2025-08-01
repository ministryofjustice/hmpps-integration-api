package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import java.io.File

class PersonResponsibleOfficerIntegrationTest : IntegrationTestBase() {
  @AfterEach
  fun resetValidators() {
    managePomCaseMockServer.resetValidator()
  }

  @Test
  fun `returns needs for a person`() {
    managePomCaseMockServer.stubForGet(
      "/api/allocation/$nomsId/primary_pom",
      File("$gatewaysFolder/managePOMcase/fixtures/GetPrimaryPOMResponse.json").readText(),
    )
    callApi("$basePath/$nomsId/person-responsible-officer")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-responsible-officer")))

    managePomCaseMockServer.assertValidationPassed()
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
