package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.prisonerbaselocation.FIXTURES_DIR
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import java.io.File

class PrisonerBaseLocationIntegrationTest : IntegrationTestBase() {
  private val knownNomisNumber = "A1234BC"
  private val unknownNomisNumnber = "Z9876YX"

  @Test
  fun `returns prisoner base location, having hmppsId being nomisID`() {
    givenKnownPrisoner()

    callApi(path = makePathPrisonerBaseLocation(knownNomisNumber))
      .andExpect(status().isOk)
      .andExpect(
        content().json(getExpectedResponse("prisoner-base-location")),
      )
  }

  @Test
  fun `does not return prisoner base location, of an unknown prisoner`() {
    givenUnknownPrisoner()

    callApi(path = makePathPrisonerBaseLocation(unknownNomisNumnber))
      .andExpect(status().isNotFound)
  }

  @Test
  fun `returns a 400 if the hmppsId is invalid`() {
    callApi(path = makePathPrisonerBaseLocation(invalidNomsId))
      .andExpect(status().isBadRequest)
  }

  private fun makePathPrisonerBaseLocation(hmppsId: String) = "$basePath/$hmppsId/prisoner-base-location"

  private fun readFixtures(fileName: String): String = File("$FIXTURES_DIR/$fileName").readText()

  private fun givenKnownPrisoner() = prisonerOffenderSearchMockServer.stubForGet("/prisoner/$knownNomisNumber", readFixtures("PrisonerByIdResponse.json"))

  private fun givenUnknownPrisoner() = prisonerOffenderSearchMockServer.stubForGet("/prisoner/$unknownNomisNumnber", readFixtures("PrisonerByIdNotFoundResponse.json"), HttpStatus.NOT_FOUND)
}
