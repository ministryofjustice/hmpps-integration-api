package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.v2

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.defaultObjectMapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.CorePersonRecord
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
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
    val response =
      callApi("$path/$nomsId")
        .andExpect(status().isOk)
        .andReturn()
        .response
        .contentAsJson<DataResponse<CorePersonRecord>>()
    response.data shouldBe expectedResponse
    corePersonRecordMockServer.assertValidationPassed()
  }

  @Test
  fun `successfully gets a record for a person using a CRN`() {
    val response =
      callApi("$path/$crn")
        .andExpect(status().isOk)
        .andReturn()
        .response
        .contentAsJson<DataResponse<CorePersonRecord>>()
    response.data shouldBe expectedResponse
    corePersonRecordMockServer.assertValidationPassed()
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
