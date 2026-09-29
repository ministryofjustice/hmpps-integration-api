package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.CaseStatusValidationException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.cemo.CemoGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.casestatus.CaseStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.casestatus.CaseStatusUpdate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cemo.CemoOrderStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@Service
class ReceiveCaseStatusService(
  private val auditService: AuditService,
  private val cemoGateway: CemoGateway,
  private val emNotificationEventPublisher: EmNotificationEventPublisher,
) {
  fun receive(
    caseId: String,
    request: CaseStatusUpdate,
  ): Response<Unit> {
    validate(caseId, request)

    val orderResponse = cemoGateway.getOrderByCaseId(caseId)
    val order = orderResponse.data
    if (order == null) {
      return Response(
        data = Unit,
        errors =
          orderResponse.errors.ifEmpty {
            listOf(UpstreamApiError(UpstreamApi.CEMO, UpstreamApiError.Type.INTERNAL_SERVER_ERROR))
          },
      )
    }

    if (order.versions.none { it.status == CemoOrderStatus.SUBMITTED }) {
      throw CaseStatusValidationException("No submitted order found for caseId $caseId")
    }

    emNotificationEventPublisher.publish(caseId, request)

    auditService.createEvent(
      "RECEIVE_CASE_STATUS_UPDATE",
      mapOf(
        "caseId" to caseId,
        "status" to request.status.value,
        "datetimeOfStatusChange" to request.datetimeOfStatusChange.toString(),
      ),
    )
    return Response(Unit)
  }

  private fun validate(
    caseId: String,
    request: CaseStatusUpdate,
  ) {
    if (caseId.isBlank()) {
      throw CaseStatusValidationException("caseId must not be blank")
    }

    if (request.status != CaseStatus.REJECTED) {
      throw CaseStatusValidationException("Only rejected case status updates are supported")
    }

    val reasons = request.reasons.orEmpty()

    if (request.status == CaseStatus.REJECTED && reasons.isEmpty()) {
      throw CaseStatusValidationException("At least one reason is required when status is rejected")
    }

    if (reasons.any { it.section == "other" && it.details.isNullOrBlank() }) {
      throw CaseStatusValidationException("details must be supplied when reason section is other")
    }
  }
}
