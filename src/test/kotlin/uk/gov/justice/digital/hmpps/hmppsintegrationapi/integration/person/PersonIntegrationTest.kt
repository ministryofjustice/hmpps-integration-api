package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class PersonIntegrationTest : IntegrationTestBase() {
  @Nested
  inner class GetPerson {
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
  }

  @Nested
  inner class GetImageMetadataForPerson {
    @Test
    fun `returns image metadata for a person`() {
      callApi("$basePath/$nomsId/images")
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("person-image-meta-data")))
    }

    @Test
    fun `images endpoint return a 404 for person in wrong prison`() {
      callApiWithCN("$basePath/$nomsId/images", limitedPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `images name endpoint return a 404 when no prisons in filter`() {
      callApiWithCN("$basePath/$nomsId/images", noPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 400 when invalid noms passed in`() {
      callApi("$basePath/$invalidNomsId/images")
        .andExpect(status().isBadRequest)
    }
  }

  @Nested
  inner class GetPersonName {
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
  }

  @Nested
  inner class GetCellLocation {
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
  }

  @Nested
  inner class GetPrisonerContacts {
    @Test
    fun `returns a prisoners contacts`() {
      val params = "?page=1&size=10"
      callApi("$basePath/$nomsId/contacts$params")
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("prisoners-contacts")))
    }
  }

  @Nested
  inner class GetIEPLevel {
    val path = "$basePath/$nomsId/iep-level"

    @Test
    fun `returns a prisoners iep level`() {
      callApi(path)
        .andExpect(status().isOk)
        .andExpect(content().json("""{"data":{"iepCode": "STD", "iepLevel": "Standard"}}"""))
    }

    @Test
    fun `return a 404 for person in wrong prison`() {
      callApiWithCN(path, limitedPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 404 when no prisons in filter`() {
      callApiWithCN(path, noPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 400 when invalid noms passed in`() {
      callApi("$basePath/$invalidNomsId/iep-level")
        .andExpect(status().isBadRequest)
    }
  }

  @Nested
  inner class GetNumberOfChildren {
    val path = "$basePath/$nomsId/number-of-children"

    @Test
    fun `returns a prisoner's number of children`() {
      callApi(path)
        .andExpect(status().isOk)
        .andExpect(
          content().json(
            """
          {
            "data": {
              "numberOfChildren": "string"
            }
          }
          """,
          ),
        )
    }

    @Test
    fun `return a 404 for person in wrong prison`() {
      callApiWithCN(path, limitedPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 404 when no prisons in filter`() {
      callApiWithCN(path, noPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 400 when invalid noms passed in`() {
      callApi("$basePath/$invalidNomsId/number-of-children")
        .andExpect(status().isBadRequest)
    }
  }

  @Nested
  inner class GetPhysicalCharacteristics {
    val path = "$basePath/$nomsId/physical-characteristics"

    @Test
    fun `returns a prisoner's physical characteristics`() {
      callApi(path)
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("physical-characteristics")))
    }

    @Test
    fun `return a 404 for person in wrong prison`() {
      callApiWithCN(path, limitedPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 404 when no prisons in filter`() {
      callApiWithCN(path, noPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 400 when invalid noms passed in`() {
      callApi("$basePath/$invalidNomsId/physical-characteristics")
        .andExpect(status().isBadRequest)
    }
  }
}
