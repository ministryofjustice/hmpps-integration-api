package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

internal class PersonIntegrationTest : IntegrationTestBase() {
  val basePath = "/v1/persons"
  final val hmppsId = "2004/13116M"
  val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)

  @Test
  fun `returns a list of persons using first name and last name as search parameters`() {
    val firstName = "Example_First_Name"
    val lastName = "Example_Last_Name"
    val queryParams = "first_name=$firstName&last_name=$lastName"

    mockMvc.perform(
      get("$basePath?$queryParams").headers(getAuthHeader()),
    )
      .andExpect(status().isOk)
      .andDo(print())
      .andExpect(content().json(getExpectedResponse("person-name-search-response")))
  }

  @Test
  fun `returns a person from Prisoner Offender Search and Probation Offender Search`() {
    mockMvc.perform(
      get("$basePath/$encodedHmppsId").headers(getAuthHeader()),
    )
      .andExpect(status().isOk)
      .andDo(print())
      .andExpect(content().json(getExpectedResponse("person-offender-and-probation-search-response")))
  }

  @Test
  fun `returns image metadata for a person`() {
    mockMvc.perform(
      get("$basePath/$encodedHmppsId/images").headers(getAuthHeader()),
    )
      .andExpect(status().isOk)
      .andDo(print())
      .andExpect(content().json(getExpectedResponse("person-image-meta-data")))
  }

  @Test
  fun `returns person name details for a person`() {
    mockMvc.perform(
      get("$basePath/$encodedHmppsId/name").headers(getAuthHeader()),
    )
      .andExpect(status().isOk)
      .andDo(print())
      .andExpect(
        content().json(
          """
        {"data":{"firstName":"string","lastName":"string"}}
      """,
        ),
      )
  }

  @Test
  fun `returns person cell location if in prison`() {
    mockMvc.perform(
      get("$basePath/$encodedHmppsId/cell-location").headers(getAuthHeader()),
    )
      .andExpect(status().isOk)
      .andDo(print())
      .andExpect(content().json("""
        {"data":{"prisonCode":"MDI","prisonName":"HMP Leeds","cell":"A-1-002"}}
      """))
  }
}
