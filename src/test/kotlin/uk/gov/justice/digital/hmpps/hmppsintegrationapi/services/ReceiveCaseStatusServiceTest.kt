package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.CaseStatusValidationException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.UpstreamApiException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CemoGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cemo.CemoOrderCaseSearchResult
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cemo.CemoOrderStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cemo.CemoOrderVersion
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.up3.CaseStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.up3.CaseStatusReason
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.up3.CaseStatusUpdate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.time.OffsetDateTime
import java.util.UUID

class ReceiveCaseStatusServiceTest :
  DescribeSpec({
    val auditService = mock<AuditService>()
    val cemoGateway = mock<CemoGateway>()
    val eventPublisher = mock<EmNotificationEventPublisher>()
    val service = ReceiveCaseStatusService(auditService, cemoGateway, eventPublisher)
    val changedAt = OffsetDateTime.parse("2023-10-27T14:30:00Z")
    val submittedOrder = CemoOrderCaseSearchResult(UUID.randomUUID(), listOf(CemoOrderVersion(CemoOrderStatus.SUBMITTED)))

    beforeTest {
      clearInvocations(auditService, cemoGateway, eventPublisher)
      whenever(cemoGateway.getOrderByCaseId(any())).thenReturn(Response(submittedOrder))
    }

    fun request(
      status: CaseStatus = CaseStatus.REJECTED,
      reasons: List<CaseStatusReason>? = listOf(CaseStatusReason("duplicate_submission", "Duplicate")),
    ) = CaseStatusUpdate(status, reasons, changedAt)

    describe("receive") {
      it("audits a valid case status update without recording reason details") {
        service.receive("case-123", request())

        verify(auditService).createEvent(
          eq("RECEIVE_UP3_CASE_STATUS"),
          eq(
            mapOf(
              "caseId" to "case-123",
              "status" to "rejected",
              "datetimeOfStatusChange" to changedAt.toString(),
            ),
          ),
        )
      }

      it("publishes when any CEMO order version is submitted") {
        whenever(cemoGateway.getOrderByCaseId(any())).thenReturn(
          Response(
            CemoOrderCaseSearchResult(
              UUID.randomUUID(),
              listOf(CemoOrderVersion(CemoOrderStatus.SUBMITTED), CemoOrderVersion(CemoOrderStatus.IN_PROGRESS)),
            ),
          ),
        )

        val update = request()
        service.receive("case-123", update)

        verify(eventPublisher).publish("case-123", update)
        verify(auditService).createEvent(eq("RECEIVE_UP3_CASE_STATUS"), any())
      }

      it("rejects an order without a submitted version") {
        whenever(cemoGateway.getOrderByCaseId(any())).thenReturn(
          Response(
            CemoOrderCaseSearchResult(UUID.randomUUID(), listOf(CemoOrderVersion(CemoOrderStatus.IN_PROGRESS))),
          ),
        )

        val exception =
          shouldThrow<CaseStatusValidationException> {
            service.receive("case-123", request())
          }

        exception.message shouldBe "No submitted order found for caseId case-123"
        verify(eventPublisher, never()).publish(any(), any())
      }

      it("returns not found when CEMO cannot find the order") {
        whenever(cemoGateway.getOrderByCaseId(any())).thenReturn(
          Response(
            data = null,
            errors = listOf(UpstreamApiError(UpstreamApi.CEMO, UpstreamApiError.Type.ENTITY_NOT_FOUND)),
          ),
        )

        shouldThrow<EntityNotFoundException> {
          service.receive("case-123", request())
        }
      }

      it("returns an upstream error when CEMO fails") {
        whenever(cemoGateway.getOrderByCaseId(any())).thenReturn(
          Response(
            data = null,
            errors = listOf(UpstreamApiError(UpstreamApi.CEMO, UpstreamApiError.Type.INTERNAL_SERVER_ERROR)),
          ),
        )

        shouldThrow<UpstreamApiException> {
          service.receive("case-123", request())
        }
      }

      it("rejects a rejected update without reasons") {
        val exception =
          shouldThrow<CaseStatusValidationException> {
            service.receive("case-123", request(reasons = emptyList()))
          }

        exception.message shouldBe "At least one reason is required when status is rejected"
      }

      it("rejects an other reason without details") {
        val exception =
          shouldThrow<CaseStatusValidationException> {
            service.receive(
              "case-123",
              request(reasons = listOf(CaseStatusReason("other"))),
            )
          }

        exception.message shouldBe "details must be supplied when reason section is other"
      }

      it("rejects a blank case id") {
        shouldThrow<CaseStatusValidationException> {
          service.receive(" ", request())
        }
      }
    }
  })
