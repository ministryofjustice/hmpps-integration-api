package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.CaseStatusValidationException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.up3.CaseStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.up3.CaseStatusReason
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.up3.CaseStatusUpdate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.time.OffsetDateTime

class ReceiveCaseStatusServiceTest :
  DescribeSpec({
    val auditService = mock<AuditService>()
    val service = ReceiveCaseStatusService(auditService)
    val changedAt = OffsetDateTime.parse("2023-10-27T14:30:00Z")

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
