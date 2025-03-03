package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.prison

import org.junit.jupiter.api.Test
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
    callApi("$basePrisonPath/prisoners?first_name=$deliberateMissVal&last_name=$lastName&dateOfBirth=$dateOfBirth")
      .andExpect(status().isOk)
  }

  @Test
  fun `return multiple prisoners when querying by complex parameters`() {
    callApi("$basePrisonPath/prisoners?first_name=$firstName&last_name=$lastName&dateOfBirth=$dateOfBirth")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("prisoners-response")))
  }
}
