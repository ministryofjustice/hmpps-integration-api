package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import jakarta.validation.ValidationException
import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.EducationStatusChangeRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.time.Instant
import java.util.UUID

private val log = KotlinLogging.logger {}

@Service
class EducationStatusService(
  private val getPersonService: GetPersonService,
  private val domainEventPublisher: DomainEventPublisher,
) {
  fun sendEducationUpdateEvent(
    hmppsId: String,
    request: EducationStatusChangeRequest,
  ): Response<HmppsMessageResponse> {
    log.debug("EducationUpdateEvent: Attempting to create domain event for education status update for hmppsId: $hmppsId")
    val personResponse = getPersonService.getNomisNumber(hmppsId = hmppsId)

    if (personResponse.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      log.debug("EducationUpdateEvent: Could not find nomis number for hmppsId: $hmppsId")
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }

    if (personResponse.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      log.debug("EducationUpdateEvent: Invalid hmppsId: $hmppsId")
      throw ValidationException("Invalid HMPPS ID: $hmppsId")
    }

    val prisonNumber = personResponse.data?.nomisNumber ?: run { throw ValidationException("Invalid HMPPS ID: $hmppsId") }

    createAndPublishCuriousEducationUpdateEvent(prisonNumber = prisonNumber, detailUrl = request.detailUrl.toString(), description = request.status.name, request.requestId)

    return Response(HmppsMessageResponse(message = "Education status update event written to queue"))
  }

  fun createAndPublishCuriousEducationUpdateEvent(
    prisonNumber: String,
    detailUrl: String,
    description: String,
    externalReference: UUID,
  ) {
    log.info { "Publishing education updated for prisoner [$prisonNumber]" }
    domainEventPublisher.createAndPublishEvent(
      prisonNumber = prisonNumber,
      occurredAt = Instant.now(),
      eventType = "prison.education.updated",
      description = description,
      detailUrl = detailUrl,
      additionalInformation = mapOf("curiousExternalReference" to externalReference),
    )
  }
}
