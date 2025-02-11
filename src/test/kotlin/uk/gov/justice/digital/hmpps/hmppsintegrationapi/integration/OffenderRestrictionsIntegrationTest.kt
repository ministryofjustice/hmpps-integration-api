package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class OffenderRestrictionsIntegrationTest : IntegrationTestBase() {
  final val prisonId = "MDI"
  final val hmppsId = "A1234BC"
  val nonAssocPrisonerPath = "/v1/prison/$prisonId/prisoners/$hmppsId/non-associations"

  @Test
  fun `return a list non associated for a prisoner`() {
    callApi(nonAssocPrisonerPath)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("")))
  }

  @Test
  fun `return a 404 when prison id is not authorised for the consumer when getting a prisoners non assocs`() {
    val wrongPrisonId = "XYZ"
    val headers = org.springframework.http.HttpHeaders()
    headers.set("subject-distinguished-name", "C=GB,ST=London,L=London,O=Home Office,CN=limited-prisons")
    mockMvc
      .perform(
        get(nonAssocPrisonerPath).headers(headers),
      ).andExpect(status().isNotFound)
  }

  @Test
  fun `return a 404 when a prisoner has no returned non assocs`() {
    val incorrectId = "2222222222222"
    val nonAssocPrisonerPathTempTest = "/v1/prison/$prisonId/prisoners/$incorrectId/non-associations"

    mockMvc
      .perform(
        get(nonAssocPrisonerPathTempTest),
      ).andExpect(status().isNotFound)
  }

  @Test
  fun `return a 400 BAD REQUEST when invalid params are supplied to non assoc prisoner endpoint`() {
    val incorrectParams = "?includeOpen=false&includeClosed=false"
    val headers = org.springframework.http.HttpHeaders()
    headers.set("subject-distinguished-name", "C=GB,ST=London,L=London,O=Home Office,CN=limited-prisons")
    mockMvc
      .perform(
        get(nonAssocPrisonerPath + incorrectParams).headers(headers),
      ).andExpect(status().isBadRequest)
  }
}
