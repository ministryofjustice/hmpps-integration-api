package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.kotlin.any
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.writeAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.SupervisionStatus
import java.io.File
import kotlin.test.assertEquals

@DisplayName("Address Integration")
@TestPropertySource(properties = ["services.ndelius.base-url=http://localhost:4201"])
class AddressIntegrationTest : IntegrationTestBase() {
  val path = "$basePath/$nomsId/addresses"
  val crnPath = "$basePath/$crn/addresses"
  val notExistsCrnPath = "$basePath/X999999/addresses"
  val notexistsNomisPath = "$basePath/X9999XX/addresses"

  @MockitoSpyBean
  lateinit var authorisationConfig: AuthorisationConfig

  @BeforeEach
  fun setup() {
    reset(authorisationConfig)
    corePersonRecordGateway.stubForGet(
      "/person/prison/$nomsId",
      File(
        "$gatewaysFolder/cpr/fixtures/core-person-record-response.json",
      ).readText(),
    )
    corePersonRecordGateway.stubForGet(
      "/person/probation/$crn",
      File(
        "$gatewaysFolder/cpr/fixtures/core-person-record-response.json",
      ).readText(),
    )
  }

  @Test
  fun `returns addresses for a person`() {
    callApi(path)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-addresses")))
  }

  @Test
  fun `returns only delius addresses for a person when crn provided and nomis number from delius not present`() {
    corePersonRecordGateway.stubForGet(
      "/person/probation/$crn",
      "not found",
      HttpStatus.NOT_FOUND,
    )

    nDeliusMockServer.stubForPost(
      "/search/probation-cases",
      writeAsJson(mapOf("crn" to crn)),
      File(
        "$gatewaysFolder/ndelius/fixtures/GetOffenderResponseNoNomis.json",
      ).readText(),
    )

    val addresses =
      callApi(crnPath)
        .andExpect(status().isOk)
        .andReturn()
        .response
        .contentAsJson<Response<List<Address>>>()
        .data
    assertEquals(1, addresses.size)
    assertEquals("From NDelius", addresses[0].notes)
  }

  @Test
  fun `returns only prison addresses for a person when nomis provided and cpr does not find a valid crn`() {
    corePersonRecordGateway.stubForGet(
      "/person/prison/$nomsId",
      File(
        "$gatewaysFolder/cpr/fixtures/core-person-record-response-incorrect-ids.json",
      ).readText(),
    )

    val addresses =
      callApi(path)
        .andExpect(status().isOk)
        .andReturn()
        .response
        .contentAsJson<Response<List<Address>>>()
        .data
    assertEquals(1, addresses.size)
    assertEquals("This is a comment text", addresses[0].notes)
  }

  @Test
  fun `returns a not found when non existent crn passed and no valid CRN and NOMIS resolved`() {
    callApi(notExistsCrnPath)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `returns a not found when non existent nomis passed and no valid CRN and NOMIS resolved`() {
    callApi(notexistsNomisPath)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `returns a 400 if the hmppsId is invalid`() {
    callApi("$basePath/$invalidNomsId/addresses")
      .andExpect(status().isBadRequest)
  }

  @Test
  fun `returns a 404 for if consumer has empty list of prisons`() {
    callApiWithCN(path, noPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `returns a 404 for prisoner in wrong prison`() {
    callApiWithCN(path, limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `returns probation addresses only when only PROBATION supervision status`() {
    whenever(authorisationConfig.allFilters(any(), anyList())).thenReturn(ConsumerFilters(supervisionStatuses = listOf(SupervisionStatus.PROBATION.name)))

    val response =
      callApi(path)
        .andExpect(status().isOk)
        .andReturn()
        .response
        .contentAsJson<Response<List<Address>>>()
    assertThat(response.data.size).isEqualTo(1)
  }

  @Test
  fun `returns prison addresses only when only PRISON supervision status`() {
    whenever(authorisationConfig.allFilters(any(), anyList())).thenReturn(ConsumerFilters(supervisionStatuses = listOf(SupervisionStatus.PRISONS.name)))
    val response =
      callApi(path)
        .andExpect(status().isOk)
        .andReturn()
        .response
        .contentAsJson<Response<List<Address>>>()
    assertThat(response.data.size).isEqualTo(1)
  }

  @Test
  fun `returns probation and prison addresses when PRISON and PROBATION supervision status`() {
    whenever(authorisationConfig.allFilters(any(), anyList())).thenReturn(ConsumerFilters(supervisionStatuses = listOf(SupervisionStatus.PRISONS.name, SupervisionStatus.PROBATION.name)))
    val response =
      callApi(path)
        .andExpect(status().isOk)
        .andReturn()
        .response
        .contentAsJson<Response<List<Address>>>()
    assertThat(response.data.size).isEqualTo(2)
  }
}
