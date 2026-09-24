package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.prison

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class LiveRollIntegrationTest : IntegrationTestBase() {
  final val path = "/v1/prison/$prisonId/live-roll"
  final val pathBad = "/v1/prison/not-found/live-roll"
  final val pathNotFound = "/v1/prison/$emptyPrisonId/live-roll"
  final val page = 1
  final val size = 10
  val pathWithQueryParams = "?page=$page&size=$size"

  @Test
  fun `return a list prisoners with all fields populated`() {
    callApi("$path$pathWithQueryParams")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("live-roll.json"), JsonCompareMode.STRICT))
  }

  @Test
  fun `missing prison results in a 404`() {
    callApi("$pathNotFound$pathWithQueryParams")
      .andExpect(status().isNotFound)
  }

  @Test
  fun `missing prison results in a 500`() {
    prisonerOffenderSearchMockServer.stubForGet(
      "/prison/not-found/prisoners?page=0&size=10",
      "",
      HttpStatus.INTERNAL_SERVER_ERROR,
    )
    callApi("$pathBad$pathWithQueryParams")
      .andExpect(status().isInternalServerError)
  }
}
