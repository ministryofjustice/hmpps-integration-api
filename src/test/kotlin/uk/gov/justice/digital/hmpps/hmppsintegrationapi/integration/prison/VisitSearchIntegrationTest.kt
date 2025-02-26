package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.prison

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class VisitSearchIntegrationTest : IntegrationTestBase() {
  private final val prisonId = "MDI"
  final val path = "/v1/prison/$prisonId/visit/search"
  final val hmppsId = "A1234AA"
  final val fromDate = "2024-01-01"
  final val toDate = "2024-01-14"
  final val visitStatus = "BOOKED"
  final val page = 1
  final val size = 10
  val pathWithQueryParams = "?visitStatus=$visitStatus&page=$page&size=$size&prisonerId=$hmppsId&fromDate=$fromDate&toDate=$toDate"

  @Test
  fun `return a prisoner with all fields populated`() {
    callApi("$path$pathWithQueryParams")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("visit-search-response")))
  }

  @Test
  fun `return a 404 for prisonId not in consumers profile prison`() {
    callApiWithCN("/v1/prison/MDI/visit/search$pathWithQueryParams", limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return a 400 for invalid query string value`() {
    val invalidStatus = "ESCAPED!!!!"
    callApi("$path?visitStatus=$invalidStatus&page=$page&size=$size&prisonerId=$hmppsId&fromDate=$fromDate&toDate=$toDate")
      .andExpect(status().isBadRequest)
  }
}
