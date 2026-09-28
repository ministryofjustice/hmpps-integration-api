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
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
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
