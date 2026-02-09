package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.prison

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import java.io.File

class PrisonIntegrationTest : IntegrationTestBase() {
  private final val hmppsId = "G2996UX"
  private final val basePrisonPath = "/v1/prison"
  private final val firstName = "John"
  private final val lastName = "Doe"
  private final val dateOfBirth = "1980-01-01"

  @AfterEach
  fun resetValidators() {
    prisonerOffenderSearchMockServer.resetValidator()
  }

  @Test
  fun `return a prisoner with all fields populated`() {
    callApi("$basePrisonPath/prisoners/$hmppsId")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("prisoner-response.json")))

    prisonerOffenderSearchMockServer.assertValidationPassed()
  }

  @Test
  fun `return a 404 for prisoner in wrong prison`() {
    callApiWithCN("$basePrisonPath/prisoners/$hmppsId", limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return a 404 no prisons in filter`() {
    callApiWithCN("$basePrisonPath/prisoners/$hmppsId", noPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return a 200 for empty but successful upstream response`() {
    val deliberateMissVal = "nope"
    prisonerOffenderSearchMockServer.stubForPost(
      "/global-search?size=9999",
      """
            {
              "firstName": "$deliberateMissVal",
              "lastName": "$lastName",
              "includeAliases": false,
              "dateOfBirth": "$dateOfBirth"
            }
          """.removeWhitespaceAndNewlines(),
      """
      {
        "content": [],
        "pageable": {
          "sort": {
            "empty": true,
            "unsorted": true,
            "sorted": false
          },
          "offset": 0,
          "pageSize": 10,
          "pageNumber": 0,
          "paged": true,
          "unpaged": false
        },
        "totalPages": 1,
        "last": false,
        "totalElements": 0,
        "size": 10,
        "number": 0,
        "sort": {
          "empty": true,
          "unsorted": true,
          "sorted": false
        },
        "first": true,
        "numberOfElements": 0,
        "empty": false
      }
      """.trimIndent(),
    )
    callApi("$basePrisonPath/prisoners?first_name=$deliberateMissVal&last_name=$lastName&date_of_birth=$dateOfBirth")
      .andExpect(status().isOk)

    prisonerOffenderSearchMockServer.assertValidationPassed()
  }

  @Test
  fun `return multiple prisoners when querying by complex parameters`() {
    val firstName = "Robert"
    val lastName = "Larsen"
    val dateOfBirth = "1975-04-02"

    prisonerOffenderSearchMockServer.stubForPost(
      "/global-search?size=9999",
      """
            {
              "firstName": "$firstName",
              "lastName": "$lastName",
              "includeAliases": false,
              "dateOfBirth": "$dateOfBirth"
            }
          """.removeWhitespaceAndNewlines(),
      File(
        "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/prisoneroffendersearch/fixtures/GetPersons.json",
      ).readText(),
    )
    callApi("$basePrisonPath/prisoners?first_name=$firstName&last_name=$lastName&date_of_birth=$dateOfBirth")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("prisoners-response")))

    prisonerOffenderSearchMockServer.assertValidationPassed()
  }
}
