package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class HmppsIdIntegrationTest : IntegrationTestBase() {
  @Test
  fun `gets the person detail`() {
    callApi("/v1/hmpps/id/nomis-number/$nomsId")
      .andExpect(status().isOk)
      .andExpect(
        content().json(
          """
        {"data":{"hmppsId":"A123456"}}
      """,
        ),
      )
  }

  @Test
  fun `gets the nomis Id for a HMPPSID where the id is a crn`() {
    callApi("/v1/hmpps/id/$crn/nomis-number")
      .andExpect(status().isOk)
      .andExpect(
        content().json(
          """
        {"data":{"nomisNumber":"G5555TT"}}
      """,
        ),
      )
  }

  @Test
  fun `gets the nomis Id for a HMPPSID where the id is a NOMIS id`() {
    callApi("/v1/hmpps/id/$nomsId/nomis-number")
      .andExpect(status().isOk)
      .andExpect(
        content().json(
          """
        {"data":{"nomisNumber":"G2996UX"}}
      """,
        ),
      )
  }

  @Test
  fun `gets the nomis Id for a HMPPSID where the id is invalid`() {
    callApi("/v1/hmpps/id/invalidId/nomis-number")
      .andExpect(status().is4xxClientError)
      .andExpect(
        content().json(
          """
        {"userMessage":"Invalid HMPPS ID: invalidId","developerMessage":"Validation failure: Invalid HMPPS ID: invalidId"}
      """,
        ),
      )
  }

  @Test
  fun `gets the nomis Id for a HMPPSID where the id not found`() {
    callApi("/v1/hmpps/id/invalidId/nomis-number")
      .andExpect(status().is4xxClientError)
      .andExpect(
        content().json(
          """
        {"userMessage":"Invalid HMPPS ID: invalidId","developerMessage":"Validation failure: Invalid HMPPS ID: invalidId","moreInfo":null}
      """,
        ),
      )
  }
}
