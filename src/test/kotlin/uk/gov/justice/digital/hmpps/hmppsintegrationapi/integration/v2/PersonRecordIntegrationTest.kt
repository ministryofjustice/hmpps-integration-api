package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.v2

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.defaultObjectMapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.CorePersonRecord
import java.io.File

class PersonRecordIntegrationTest : IntegrationTestBase() {
  val path = "/v2/persons"
  val upstreamResponse =
    File(
      "$gatewaysFolder/cpr/fixtures/core-person-record-response.json",
    ).readText()

  val expectedResponse = defaultObjectMapper.readValue(upstreamResponse, CorePersonRecord::class.java)

  @BeforeEach
  fun setup() {
    corePersonRecordMockServer.stubForGet(
      "/person/prison/$nomsId",
      body = upstreamResponse,
    )
    corePersonRecordMockServer.stubForGet(
      "/person/probation/$crn",
      body = upstreamResponse,
    )
  }

  @Test
  fun `successfully gets a record for a person using a NOMIS ID`() {
    callApi("$path/$nomsId")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-record-response.json"), JsonCompareMode.STRICT))
  }

  @Test
  fun `successfully gets a record for a person using a CRN`() {
    callApi("$path/$crn")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-record-response.json"), JsonCompareMode.STRICT))
  }

  @Test
  fun `returns a 400 bad request when hmpps id is neither a CRN or a NOMIS ID`() {
    callApi("$path/99AABBCC").andExpect(status().isBadRequest)
  }

  @Test
  fun `returns a 404 not found when crn not found`() {
    callApi("$path/F123456").andExpect(status().isNotFound)
  }

  @Test
  fun `returns a 404 not found when nomis id not found`() {
    callApi("$path/F1234BC").andExpect(status().isNotFound)
  }
}
