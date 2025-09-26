package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_CONTACT_EVENTS_ENDPOINT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.USE_STUBBED_CONTACT_EVENTS_DATA
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.writeAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactEvent
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.ContactEventStubGenerator.generateNDeliusContactEvent
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.ContactEventStubGenerator.generateNDeliusContactEvents
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.PaginatedResponse
import java.io.File
import kotlin.test.assertEquals

// Using a temporary Mock Server as this service cannot use prism mocks
@TestPropertySource(properties = ["services.ndelius.base-url=http://localhost:4201"])
class ContactEventsIntegrationTest : IntegrationTestBase() {
  @MockitoBean
  private lateinit var featureFlagConfig: FeatureFlagConfig

  @AfterEach
  fun resetValidators() {
    prisonerOffenderSearchMockServer.resetValidator()
  }

  @BeforeEach
  fun resetMocks() {
    nDeliusMockServer.resetAll()
    whenever(featureFlagConfig.getConfigFlagValue(USE_CONTACT_EVENTS_ENDPOINT)).thenReturn(true)
    prisonerOffenderSearchMockServer.stubForGet(
      "/prisoner/$nomsId",
      File(
        "$gatewaysFolder/prisoneroffendersearch/fixtures/PrisonerByIdResponse.json",
      ).readText(),
    )
    prisonerOffenderSearchMockServer.stubForGet(
      "/prisoner/$nomsIdFromProbation",
      File(
        "$gatewaysFolder/prisoneroffendersearch/fixtures/PrisonerByIdResponse.json",
      ).readText(),
    )

    nDeliusMockServer.stubForPost(
      "/search/probation-cases",
      writeAsJson(mapOf("nomsNumber" to nomsId)),
      getExpectedResponse("offender.json"),
    )
  }

  @Test
  fun `returns first page of contact events from delius with default page params with correct dates`() {
    nDeliusMockServer.stubForGet(
      "/case/$crn/contacts?page=1&size=10",
      writeAsJson(generateNDeliusContactEvents(crn = crn, pageSize = 10, pageNumber = 1, totalRecords = 100)),
    )

    val response =
      callApi("$basePath/$nomsId/contact-events")
        .andExpect(status().isOk)
        .andReturn()
        .response
        .contentAsJson<PaginatedResponse<ContactEvent>>()
    assertEquals(10, response.data.size)
    assertEquals(1, response.pagination.page)
    assertEquals(false, response.pagination.isLastPage)
    assertEquals(10, response.pagination.count)
    assertEquals(10, response.pagination.totalPages)
    assertEquals(100, response.pagination.totalCount)
  }

  @Test
  fun `returns last page of contact events from delius with specified page params`() {
    val expectedApiCreationDateTime = "2025-08-29T10:34:03.569Z"
    val expectedApiUpdateDateTime = "2025-08-30T10:34:03.569Z"
    val expectedApiContactDateTime = "2025-08-31T10:34:03.569Z"

    nDeliusMockServer.stubForGet(
      "/case/$crn/contacts?page=4&size=3",
      writeAsJson(generateNDeliusContactEvents(crn = crn, pageSize = 3, pageNumber = 4, totalRecords = 10)),
    )

    val response =
      callApi("$basePath/$nomsId/contact-events?page=4&perPage=3")
        .andExpect(status().isOk)
        .andReturn()
        .response.contentAsString

    assertEquals(1, JsonPath.parse(response).read("$.data.length()"))
    assertEquals(4, JsonPath.parse(response).read("$.pagination.page"))
    assertEquals(true, JsonPath.parse(response).read("$.pagination.isLastPage"))
    assertEquals(1, JsonPath.parse(response).read("$.pagination.count"))
    assertEquals(4, JsonPath.parse(response).read("$.pagination.totalPages"))
    assertEquals(10, JsonPath.parse(response).read("$.pagination.totalCount"))

    assertEquals(expectedApiCreationDateTime, JsonPath.parse(response).read("$.data[0].creationDateTime"))
    assertEquals(expectedApiUpdateDateTime, JsonPath.parse(response).read("$.data[0].updateDateTime"))
    assertEquals(expectedApiContactDateTime, JsonPath.parse(response).read("$.data[0].contactDateTime"))

    /*
      val creationDateTime: LocalDateTime,
  val updateDateTime: LocalDateTime,
  val contactDateTime: LocalDateTime,
     */
  }

  @Test
  fun `returns 404 when ndelius returns a 404 for contact events`() {
    callApi("$basePath/$nomsId/contact-events?page=4&perPage=3")
      .andExpect(status().isNotFound)
  }

  @Test
  fun `returns 404 when no probation identifier returns a 404 for contact events`() {
    nDeliusMockServer.stubForPost(
      "/search/probation-cases",
      writeAsJson(mapOf("nomsNumber" to nomsId)),
      getExpectedResponse("offender-no-crn.json"),
    )
    callApi("$basePath/$nomsId/contact-events?page=4&perPage=3")
      .andExpect(status().isNotFound)
  }

  @Test
  fun `returns 400 when ndelius returns a 400 for contact events`() {
    nDeliusMockServer.stubForGet(
      "/case/$crn/contacts?page=4&size=3",
      "",
      HttpStatus.BAD_REQUEST,
    )
    callApi("$basePath/$nomsId/contact-events?page=4&perPage=3")
      .andExpect(status().isBadRequest)
  }

  @Test
  fun `returns a contact event for a person with contact event id with correct date conversion`() {
    val nDeliusContactEvent = generateNDeliusContactEvent(crn = crn, id = 1)

    val expectedDeliusDate = "2025-09-07T11:34:03.569"
    val expectedApiDate = "2025-09-07T10:34:03.569Z"

    val deliusStub = writeAsJson(nDeliusContactEvent)
    nDeliusMockServer.stubForGet(
      "/case/$crn/contacts/1",
      deliusStub,
    )

    val response =
      callApi("$basePath/$nomsId/contact-events/1")
        .andExpect(status().isOk)
        .andReturn()
        .response.contentAsString

    assertEquals(expectedDeliusDate, JsonPath.parse(deliusStub).read("$.creationDateTime"))
    assertEquals(expectedApiDate, JsonPath.parse(response).read("$.data.creationDateTime"))
  }

  @Test
  fun `returns a 503 for get contact events when feature flag is not enabled`() {
    whenever(featureFlagConfig.getConfigFlagValue(USE_CONTACT_EVENTS_ENDPOINT)).thenReturn(false)
    callApi("$basePath/$nomsId/contact-events").andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `returns a 503 for get contact event when feature flag is not enabled`() {
    whenever(featureFlagConfig.getConfigFlagValue(USE_CONTACT_EVENTS_ENDPOINT)).thenReturn(false)
    callApi("$basePath/$nomsId/contact-events/1").andExpect(status().isServiceUnavailable)
  }

  @Test
  fun `returns stubbed data based on the pagination parameters for contact events`() {
    whenever(featureFlagConfig.isEnabled(USE_STUBBED_CONTACT_EVENTS_DATA)).thenReturn(true)
    val response =
      callApi("$basePath/$nomsId/contact-events?page=5&perPage=2")
        .andExpect(status().isOk)
        .andReturn()
        .response
        .contentAsJson<PaginatedResponse<ContactEvent>>()
    assertEquals(2, response.data.size)
    assertEquals(5, response.pagination.page)
    assertEquals(true, response.pagination.isLastPage)
    assertEquals(2, response.pagination.count)
    assertEquals(5, response.pagination.totalPages)
    assertEquals(10, response.pagination.totalCount)
  }

  @Test
  fun `returns stubbed data based on the id for contact event`() {
    whenever(featureFlagConfig.isEnabled(USE_STUBBED_CONTACT_EVENTS_DATA)).thenReturn(true)
    val response =
      callApi("$basePath/$nomsId/contact-events/15")
        .andExpect(status().isOk)
        .andReturn()
        .response
        .contentAsJson<Response<ContactEvent>>()
    assertEquals(15, response.data.contactEventIdentifier)
  }
}
