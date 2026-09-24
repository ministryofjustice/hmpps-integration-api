package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.WebMvcTestConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CemoGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cemo.CemoOrderCaseSearchResult
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cemo.CemoOrderStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cemo.CemoOrderVersion
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.EmNotificationEventPublisher
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.ReceiveCaseStatusService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.util.UUID

@WebMvcTest(controllers = [CaseStatusController::class])
@Import(WebMvcTestConfiguration::class, ReceiveCaseStatusService::class)
@ActiveProfiles("test")
class CaseStatusControllerTest(
  private val mockMvc: MockMvc,
  @MockitoBean val featureFlagConfig: FeatureFlagConfig,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val cemoGateway: CemoGateway,
  @MockitoBean val emNotificationEventPublisher: EmNotificationEventPublisher,
) : DescribeSpec({
    val apiPath = "/v1/cases/case-123/status"
    val requestBody =
      """
      {
        "status": "rejected",
        "reasons": [
          {
            "section": "duplicate_submission",
            "details": "Case was already submitted."
          }
        ],
        "datetimeOfStatusChange": "2023-10-27T14:30:00Z"
      }
      """.trimIndent()

    beforeTest {
      whenever(featureFlagConfig.getConfigFlagValue(FeatureFlagConfig.UP3_CASE_STATUS_UPDATE_ENABLED)).thenReturn(true)
      whenever(cemoGateway.getOrderByCaseId("case-123")).thenReturn(
        Response(
          CemoOrderCaseSearchResult(
            UUID.randomUUID(),
            listOf(CemoOrderVersion(CemoOrderStatus.SUBMITTED)),
          ),
        ),
      )
    }

    describe("PUT /v1/cases/{caseId}/status") {
      it("returns 200 with no body for an authorized valid request") {
        val response =
          mockMvc
            .perform(
              put(apiPath)
                .header("subject-distinguished-name", "C=GB,O=Home Office,CN=automated-test-client")
                .header("cert-serial-number", "9572494320151578633330348943480876283449388176")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody),
            ).andReturn()
            .response

        response.status shouldBe HttpStatus.OK.value()
        response.contentAsString shouldBe ""
        verify(auditService).createEvent(
          eq("RECEIVE_UP3_CASE_STATUS"),
          any<Map<String, String?>>(),
        )
      }

      it("returns 403 when the request has no consumer identity") {
        val response = mockMvc.perform(put(apiPath)).andReturn().response

        response.status shouldBe HttpStatus.FORBIDDEN.value()
      }

      it("returns 422 for a rejected update without a reason") {
        val response =
          mockMvc
            .perform(
              put(apiPath)
                .header("subject-distinguished-name", "C=GB,O=Home Office,CN=automated-test-client")
                .header("cert-serial-number", "9572494320151578633330348943480876283449388176")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                  """
                  {
                    "status": "rejected",
                    "datetimeOfStatusChange": "2023-10-27T14:30:00Z"
                  }
                  """.trimIndent(),
                ),
            ).andReturn()
            .response

        response.status shouldBe HttpStatus.UNPROCESSABLE_ENTITY.value()
      }

      it("returns 400 for an unsupported status") {
        val response =
          mockMvc
            .perform(
              put(apiPath)
                .header("subject-distinguished-name", "C=GB,O=Home Office,CN=automated-test-client")
                .header("cert-serial-number", "9572494320151578633330348943480876283449388176")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody.replace("\"rejected\"", "\"unknown\"")),
            ).andReturn()
            .response

        response.status shouldBe HttpStatus.BAD_REQUEST.value()
      }

      it("returns 404 when CEMO cannot find the case") {
        whenever(cemoGateway.getOrderByCaseId("case-123")).thenReturn(
          Response(null, listOf(UpstreamApiError(UpstreamApi.CEMO, UpstreamApiError.Type.ENTITY_NOT_FOUND))),
        )

        val response =
          mockMvc
            .perform(
              put(apiPath)
                .header("subject-distinguished-name", "C=GB,O=Home Office,CN=automated-test-client")
                .header("cert-serial-number", "9572494320151578633330348943480876283449388176")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody),
            ).andReturn()
            .response

        response.status shouldBe HttpStatus.NOT_FOUND.value()
        response.contentAsString.contains("No order found for caseId case-123") shouldBe true
      }

      it("returns 500 when CEMO fails") {
        whenever(cemoGateway.getOrderByCaseId("case-123")).thenReturn(
          Response(null, listOf(UpstreamApiError(UpstreamApi.CEMO, UpstreamApiError.Type.INTERNAL_SERVER_ERROR))),
        )

        val response =
          mockMvc
            .perform(
              put(apiPath)
                .header("subject-distinguished-name", "C=GB,O=Home Office,CN=automated-test-client")
                .header("cert-serial-number", "9572494320151578633330348943480876283449388176")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody),
            ).andReturn()
            .response

        response.status shouldBe HttpStatus.INTERNAL_SERVER_ERROR.value()
      }
    }
  })
