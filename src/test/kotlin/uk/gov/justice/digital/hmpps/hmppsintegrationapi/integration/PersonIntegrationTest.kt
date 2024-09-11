package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import java.io.File
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

    val expectedResponse = File("./src/test/resources/expected-responses/person-name-search-response").readText(Charsets.UTF_8).removeWhitespaceAndNewlines()

    mockMvc.perform(
      get("$basePath?$queryParams").headers(getAuthHeader()),
    )
      .andExpect(status().isOk)
      .andDo(print())
      .andExpect(content().json(expectedResponse))
  }

  @Test
  fun `returns a person from Prisoner Offender Search and Probation Offender Search`() {
    val expectedResponse = File("./src/test/resources/expected-responses/person-offender-and-probation-search-response").readText(Charsets.UTF_8).removeWhitespaceAndNewlines()

    mockMvc.perform(
      get("$basePath/$encodedHmppsId").headers(getAuthHeader()),
    )
      .andExpect(status().isOk)
      .andDo(print())
      .andExpect(content().json(expectedResponse))
  }
}
