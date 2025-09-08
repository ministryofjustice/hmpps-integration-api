package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactEvent
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
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
      MockMvcExtensions.writeAsJson(mapOf("nomsNumber" to nomsId)),
      getExpectedResponse("offender.json"),
    )
  }

  @Test
  fun `returns first page of contact events from delius with default page params`() {
    nDeliusMockServer.stubForGet(
      "/case/$crn/contacts?page=1&size=10",
      MockMvcExtensions.writeAsJson(generateNDeliusContactEvents(crn = crn, pageSize = 10, pageNumber = 1, totalRecords = 100)),
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
    nDeliusMockServer.stubForGet(
      "/case/$crn/contacts?page=4&size=3",
      MockMvcExtensions.writeAsJson(generateNDeliusContactEvents(crn = crn, pageSize = 3, pageNumber = 4, totalRecords = 10)),
    )

    val response =
      callApi("$basePath/$nomsId/contact-events?page=4&perPage=3")
        .andExpect(status().isOk)
        .andReturn()
        .response
        .contentAsJson<PaginatedResponse<ContactEvent>>()
    assertEquals(1, response.data.size)
    assertEquals(4, response.pagination.page)
    assertEquals(true, response.pagination.isLastPage)
    assertEquals(1, response.pagination.count)
    assertEquals(4, response.pagination.totalPages)
    assertEquals(10, response.pagination.totalCount)
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
      MockMvcExtensions.writeAsJson(mapOf("nomsNumber" to nomsId)),
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
  fun `returns a contact event for a person with contact event id`() {
    val nDeliusContactEvent = generateNDeliusContactEvent(crn = crn, id = 1)
    nDeliusMockServer.stubForGet(
      "/case/$crn/contacts/1",
      MockMvcExtensions.writeAsJson(nDeliusContactEvent),
    )

    val response =
      callApi("$basePath/$nomsId/contact-events/1")
        .andExpect(status().isOk)
        .andReturn()
        .response
        .contentAsJson<DataResponse<ContactEvent>>()
    assertEquals(nDeliusContactEvent.toContactEvent().contactEventIdentifier, response.data.contactEventIdentifier)
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
