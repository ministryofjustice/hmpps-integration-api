package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.prison

import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class PrisonIntegrationTest : IntegrationTestBase() {
  private final val hmppsId = "G2996UX"
  private final val basePrisonPath = "/v1/prison"
  private final val firstName = "John"
  private final val lastName = "Doe"
  private final val dateOfBirth = "1980-01-01"

  @Test
  fun `return a prisoner with all fields populated`() {
    callApi("$basePrisonPath/prisoners/$hmppsId")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("prisoner-response")))
  }

  @Test
  fun `return a 404 for prisoner in wrong prison`() {
    val headers = HttpHeaders()
    headers.set("subject-distinguished-name", "C=GB,ST=London,L=London,O=Home Office,CN=limited-prisons")
    mockMvc.perform(
      get("$basePrisonPath/prisoners/$hmppsId").headers(headers),
    )
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return multiple prisoners when querying by complex parameters`() {
    callApi("$basePrisonPath/prisoners?first_name=$firstName&last_name=$lastName&dateOfBirth=$dateOfBirth")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("prisoners-response")))
  }
}
