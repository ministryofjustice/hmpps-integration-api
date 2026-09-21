package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.CaseStatusValidationException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.up3.CaseStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.up3.CaseStatusUpdate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@Service
class ReceiveCaseStatusService(
  private val auditService: AuditService,
) {
  fun receive(
    caseId: String,
    request: CaseStatusUpdate,
  ) {
    validate(caseId, request)

    auditService.createEvent(
      "RECEIVE_UP3_CASE_STATUS",
      mapOf(
        "caseId" to caseId,
        "status" to request.status.value,
        "datetimeOfStatusChange" to request.datetimeOfStatusChange.toString(),
      ),
    )

    // TODO
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
