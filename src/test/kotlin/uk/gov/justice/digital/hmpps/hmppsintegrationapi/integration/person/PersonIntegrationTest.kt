package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Test
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

  // Get persons name tests
  @Test
  fun `returns person name details for a person`() {
    callApi("$basePath/$nomsId/name")
      .andExpect(status().isOk)
      .andExpect(
        content().json(
          """
        {"data":{"firstName":"string","lastName":"string"}}
      """,
        ),
      )
  }

  @Test
  fun `persons name endpoint return a 404 for person in wrong prison`() {
    callApiWithCN("$basePath/$nomsId/name", limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `persons name endpoint return a 404 when no prisons in filter`() {
    callApiWithCN("$basePath/$nomsId/name", noPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `persons name endpoint return a 400 when invalid noms passed in`() {
    callApi("$basePath/$invalidNomsId/name")
      .andExpect(status().isBadRequest)
  }

  // Cell Location tests
  @Test
  fun `returns person cell location if in prison`() {
    callApi("$basePath/$nomsId/cell-location")
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
  fun `cell location return a 404 for person in wrong prison`() {
    callApiWithCN("$basePath/$nomsId/cell-location", limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `cell location return a 404 when no prisons in filter`() {
    callApiWithCN("$basePath/$nomsId/cell-location", noPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `cell location return a 400 when invalid noms passed in`() {
    callApi("$basePath/$invalidNomsId/cell-location")
      .andExpect(status().isBadRequest)
  }

  // Prisoner Contacts
  @Test
  fun `returns a prisoners contacts`() {
    val params = "?page=1&size=10"
    callApi("$basePath/$nomsId/contacts$params")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("prisoners-contacts")))
  }

  // IEP Level tests
  @Test
  fun `returns a prisoners iep level`() {
    callApi("$basePath/$nomsId/iep-level")
      .andExpect(status().isOk)
      .andExpect(content().json("""{"data":{"iepCode": "STD", "iepLevel": "Standard"}}"""))
  }

  @Test
  fun `return a 404 for person in wrong prison`() {
    callApiWithCN("$basePath/$nomsId/iep-level", limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return a 404 when no prisons in filter`() {
    callApiWithCN("$basePath/$nomsId/iep-level", noPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return a 400 when invalid noms passed in`() {
    callApi("$basePath/$invalidNomsId/iep-level")
      .andExpect(status().isBadRequest)
  }
}
