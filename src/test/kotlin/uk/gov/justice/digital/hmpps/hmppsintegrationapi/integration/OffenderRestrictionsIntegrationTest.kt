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
      .andExpect(content().json(getExpectedResponse("prisoner-non-associated")))
  }

  @Test
  fun `return a 404 when prison id is not authorised for the consumer when getting a prisoners non assocs`() {
    callApiWithCN(nonAssocPrisonerPath, limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return a 400 BAD REQUEST when invalid params are supplied to non assoc prisoner endpoint`() {
    val incorrectParams = "?includeOpen=false&includeClosed=false"
    callApi(nonAssocPrisonerPath + incorrectParams)
      .andExpect(status().isBadRequest)
  }

  @Test
  fun `return a 404 when no prisons in filter`() {
    callApiWithCN(nonAssocPrisonerPath, noPrisonsCn)
      .andExpect(status().isNotFound)
  }
}
