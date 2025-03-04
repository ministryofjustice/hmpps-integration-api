package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class PersonIntegrationTest : IntegrationTestBase() {
  @Test
  fun `returns a list of persons using first name and last name as search parameters`() {
    val firstName = "Example_First_Name"
    val lastName = "Example_Last_Name"
    val queryParams = "first_name=$firstName&last_name=$lastName"

    callApi("$basePath?$queryParams")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-name-search-response")))
  }

  @Test
  fun `returns a person from Prisoner Offender Search and Probation Offender Search`() {
    callApi("$basePath/$pnc")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-offender-and-probation-search-response")))
  }

  @Test
  fun `returns image metadata for a person`() {
    callApi("$basePath/$pnc/images")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-image-meta-data")))
  }

  @Test
  fun `returns person name details for a person`() {
    callApi("$basePath/$pnc/name")
      .andExpect(status().isOk)
      .andExpect(
        content().json(
          """
        {"data":{"firstName":"string","lastName":"string"}}
      """,
        ),
      )
  }

  @ParameterizedTest
  @ValueSource(strings = ["2004%2F13116M", "G2996UX"])
  fun `returns person cell location if in prison`(hmppsId: String) {
    callApi("$basePath/$hmppsId/cell-location")
      .andExpect(status().isOk)
      .andExpect(
        content().json(
          """
        {"data":{"prisonCode":"MDI","prisonName":"HMP Leeds","cell":"A-1-002"}}
      """,
        ),
      )
  }

  @Test
  fun `returns a prisoners contacts`() {
    val params = "?page=1&size=10"
    callApi("$basePath/$nomsId/contacts$params")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("prisoners-contacts")))
  }
}
