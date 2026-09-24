package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.CaseStatusValidationException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.UpstreamApiException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CemoGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cemo.CemoOrderStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.up3.CaseStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.up3.CaseStatusUpdate
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
  ) {
    validate(caseId, request)

    val orderResponse = cemoGateway.getOrderByCaseId(caseId)
    val order =
      orderResponse.data
        ?: throwCemoError(caseId, orderResponse.errors)

    if (order.versions.none { it.status == CemoOrderStatus.SUBMITTED }) {
      throw CaseStatusValidationException("No submitted order found for caseId $caseId")
    }

    emNotificationEventPublisher.publish(caseId, request)

    auditService.createEvent(
      "RECEIVE_UP3_CASE_STATUS",
      mapOf(
        "caseId" to caseId,
        "status" to request.status.value,
        "datetimeOfStatusChange" to request.datetimeOfStatusChange.toString(),
      ),
    )
  }

  private fun throwCemoError(
    caseId: String,
    errors: List<UpstreamApiError>,
  ): Nothing {
    val error = errors.firstOrNull()
    if (error?.type == UpstreamApiError.Type.ENTITY_NOT_FOUND) {
      throw EntityNotFoundException("No order found for caseId $caseId")
    }

    throw UpstreamApiException(
      upstreamApi = error?.causedBy ?: UpstreamApi.CEMO,
      errorType = error?.type ?: UpstreamApiError.Type.INTERNAL_SERVER_ERROR,
      resourceType = "order",
      resourceId = caseId,
      errors = errors,
    )
  }

  private fun validate(
    caseId: String,
    request: CaseStatusUpdate,
  ) {
    if (caseId.isBlank()) {
      throw CaseStatusValidationException("caseId must not be blank")
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
