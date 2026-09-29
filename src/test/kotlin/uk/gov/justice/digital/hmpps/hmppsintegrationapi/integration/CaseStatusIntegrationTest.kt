package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.EmNotificationEventPublisher

class CaseStatusIntegrationTest : IntegrationTestBase() {
  @MockitoBean
  private lateinit var emNotificationEventPublisher: EmNotificationEventPublisher

  @MockitoBean
  override lateinit var featureFlagConfig: FeatureFlagConfig

  @BeforeEach
  fun setUp() {
    cemoMockServer.resetAll()
    cemoMockServer.resetValidator()

    whenever(featureFlagConfig.getConfigFlagValue(FeatureFlagConfig.CASE_STATUS_UPDATE_ENABLED))
      .thenReturn(true)
    doNothing().whenever(emNotificationEventPublisher).publish(any(), any())
  }

  @Test
  fun `validates a case through CEMO and publishes the status update`() {
    val caseId = "case-123"
    val cemoPath = "/api/orders/search/by-case-id/$caseId"
    cemoMockServer.stubForGet(cemoPath, submittedOrderResponse)

    putApi(
      "/v1/cases/$caseId/status",
      """
      {
        "status": "rejected",
        "reasons": [{"section": "duplicate_submission", "details": "Already submitted"}],
        "datetimeOfStatusChange": "2023-10-27T14:30:00Z"
      }
      """.trimIndent(),
    ).andExpect(status().isOk)

    cemoMockServer.verify(getRequestedFor(urlEqualTo(cemoPath)))
    cemoMockServer.assertValidationPassed()
    verify(emNotificationEventPublisher).publish(eq(caseId), any())
  }

  @Test
  fun `returns 400 when CEMO rejects the case ID`() {
    val caseId = "case-123"
    val cemoPath = "/api/orders/search/by-case-id/$caseId"
    cemoMockServer.stubForGet(cemoPath, "", HttpStatus.BAD_REQUEST)

    sendValidStatusUpdate(caseId)
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$.userMessage").value("Invalid caseId $caseId"))

    verifyNoInteractions(emNotificationEventPublisher)
  }

  @Test
  fun `returns 404 when CEMO cannot find the case`() {
    val caseId = "case-123"
    val cemoPath = "/api/orders/search/by-case-id/$caseId"
    cemoMockServer.stubForGet(cemoPath, "", HttpStatus.NOT_FOUND)

    sendValidStatusUpdate(caseId)
      .andExpect(status().isNotFound)
      .andExpect(jsonPath("$.userMessage").value("No order found for caseId $caseId"))

    verifyNoInteractions(emNotificationEventPublisher)
  }

  @Test
  fun `returns 422 when CEMO has no submitted order version`() {
    val caseId = "case-123"
    val cemoPath = "/api/orders/search/by-case-id/$caseId"
    cemoMockServer.stubForGet(cemoPath, submittedOrderResponse.replace("SUBMITTED", "IN_PROGRESS"))

    sendValidStatusUpdate(caseId)
      .andExpect(status().isUnprocessableEntity)
      .andExpect(jsonPath("$.userMessage").value("No submitted order found for caseId $caseId"))

    verifyNoInteractions(emNotificationEventPublisher)
  }

  @Test
  fun `returns 500 when CEMO fails`() {
    val caseId = "case-123"
    val cemoPath = "/api/orders/search/by-case-id/$caseId"
    cemoMockServer.stubForGet(cemoPath, "", HttpStatus.INTERNAL_SERVER_ERROR)

    sendValidStatusUpdate(caseId).andExpect(status().isInternalServerError)

    verifyNoInteractions(emNotificationEventPublisher)
  }

  private fun sendValidStatusUpdate(
    caseId: String,
    statusValue: String = "rejected",
  ) = putApi(
    "/v1/cases/$caseId/status",
    """
    {
      "status": "$statusValue",
      "reasons": [{"section": "duplicate_submission", "details": "Already submitted"}],
      "datetimeOfStatusChange": "2023-10-27T14:30:00Z"
    }
    """.trimIndent(),
  )

  @Test
  fun `rejects a recognized status that the returns consumer does not support`() {
    val caseId = "case-123"

    sendValidStatusUpdate(caseId, "approved")
      .andExpect(status().isUnprocessableEntity)
      .andExpect(jsonPath("$.userMessage").value("Only rejected case status updates are supported"))

    cemoMockServer.verify(0, getRequestedFor(urlEqualTo("/api/orders/search/by-case-id/$caseId")))
    verifyNoInteractions(emNotificationEventPublisher)
  }

  @Test
  fun `rejects an unrecognised status with 400 without calling CEMO or publishing`() {
    val caseId = "case-123"

    putApi(
      "/v1/cases/$caseId/status",
      """
      {
        "status": "banana",
        "reasons": [{"section": "duplicate_submission", "details": "Already submitted"}],
        "datetimeOfStatusChange": "2023-10-27T14:30:00Z"
      }
      """.trimIndent(),
    ).andExpect(status().isBadRequest)

    cemoMockServer.verify(0, getRequestedFor(urlEqualTo("/api/orders/search/by-case-id/$caseId")))
    verifyNoInteractions(emNotificationEventPublisher)
  }

  companion object {
    private val cemoMockServer = ApiMockServer.create(UpstreamApi.CEMO)
    private val submittedOrderResponse =
      """
      {
        "id": "11111111-1111-1111-1111-111111111111",
        "versions": [
          {
            "id": "22222222-2222-2222-2222-222222222222",
            "versionId": 1,
            "additionalDocuments": [],
            "addresses": [],
            "curfewTimeTable": [],
            "dapoClauses": [],
            "enforcementZoneConditions": [],
            "isValid": true,
            "mandatoryAttendanceConditions": [],
            "offences": [],
            "status": "SUBMITTED",
            "type": "REQUEST",
            "username": "test-user",
            "dataDictionaryVersion": "DDV5"
          }
        ]
      }
      """.trimIndent()

    @BeforeAll
    @JvmStatic
    fun startCemoMockServer() {
      cemoMockServer.start()
    }

    @AfterAll
    @JvmStatic
    fun stopCemoMockServer() {
      cemoMockServer.stop()
    }
  }
}
