package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.prison

import org.junit.jupiter.api.Test
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class PrisonEventsIntegrationTest : IntegrationTestBase() {
  final val path = "/v1/prison/$prisonId/events?fromDateTime=$dateTimeNow"
  final val invalidHmppsPath = "/v1/prison/$prisonId/events?fromDateTime=$dateTimeNow&hmppsId=$invalidNomsId"
  final val invalidPrisonPath = "/v1/prison/123/events?fromDateTime=$dateTimeNow"
  final val invalidTimePath = "/v1/prison/$prisonId/events?fromDateTime=abc"

  @Test
  fun `return a list of events to a prison`() {
    callApi(path)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("prison-events.json"), JsonCompareMode.STRICT))
  }

  @Test
  fun `return a 404 with invalid hmpps Id`() {
    callApi(invalidHmppsPath)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return a 404 with invalid prison Id`() {
    callApi(invalidPrisonPath)
      .andExpect(status().isNotFound)
  }
}
