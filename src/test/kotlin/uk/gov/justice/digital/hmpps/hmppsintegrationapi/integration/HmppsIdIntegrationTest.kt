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
  fun `gets a 400 if passed an invalid Nomis number`() {
    callApi("/v1/hmpps/id/nomis-number/$invalidNomsId")
      .andExpect(status().isBadRequest)
  }

  @Test
  fun `return a 404 when prison id is not authorised for the consumer`() {
    callApiWithCN("/v1/hmpps/id/nomis-number/$nomsId", limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return a 404 when no prisons in filter`() {
    callApiWithCN("/v1/hmpps/id/nomis-number/$nomsId", noPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `gets the person detail using new url`() {
    callApi("/v1/hmpps/id/by-nomis-number/$nomsId")
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
    callApi("/v1/hmpps/id/nomis-number/by-hmpps-id/$crn")
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
    callApi("/v1/hmpps/id/nomis-number/by-hmpps-id/$nomsId")
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
  fun `gets the nomis Id for a HMPPSID where the id is a NOMIS id AND is not in delius`() {
    callApi("/v1/hmpps/id/nomis-number/by-hmpps-id/$nomsIdNotInDelius")
      .andExpect(status().isOk)
      .andExpect(
        content().json(
          """
        {"data":{"nomisNumber":"A1234AA"}}
      """,
        ),
      )
  }

  @Test
  fun `gets the nomis Id for a HMPPSID where the id is invalid`() {
    callApi("/v1/hmpps/id/nomis-number/by-hmpps-id/invalidId")
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
    callApi("/v1/hmpps/id/nomis-number/by-hmpps-id/invalidId")
      .andExpect(status().is4xxClientError)
      .andExpect(
        content().json(
          """
        {"userMessage":"Invalid HMPPS ID: invalidId","developerMessage":"Validation failure: Invalid HMPPS ID: invalidId","moreInfo":null}
      """,
        ),
      )
  }

  @Test
  fun `gets the nomis id, return a 404 for person in wrong prison`() {
    callApiWithCN("/v1/hmpps/id/nomis-number/by-hmpps-id/$nomsId", limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `gets the nomis, return a 404 when no prisons in filter`() {
    callApiWithCN("/v1/hmpps/id/nomis-number/by-hmpps-id/$nomsId", noPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `gets the nomis, return a 400 when invalid noms passed in`() {
    callApi("/v1/hmpps/id/nomis-number/by-hmpps-id/$invalidNomsId")
      .andExpect(status().isBadRequest)
  }
}
