package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import jakarta.validation.ValidationException
import mu.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.EducationALNAssessmentsChangeRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.time.Instant
import java.util.UUID

private val log = KotlinLogging.logger {}

@Service
class EducationALNAssessmentService(
  private val getPersonService: GetPersonService,
  private val domainEventPublisher: DomainEventPublisher,
) {
  fun sendEducationALNUpdateEvent(
    hmppsId: String,
    request: EducationALNAssessmentsChangeRequest,
  ): Response<HmppsMessageResponse> {
    log.debug("EducationALNUpdateEvent: Attempting to create domain event for education ALN update for hmppsId: $hmppsId")
    val personResponse = getPersonService.getNomisNumber(hmppsId = hmppsId)

    if (personResponse.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND)) {
      log.debug("EducationALNUpdateEvent: Could not find nomis number for hmppsId: $hmppsId")
      throw EntityNotFoundException("Could not find person with id: $hmppsId")
    }

    if (personResponse.hasError(UpstreamApiError.Type.BAD_REQUEST)) {
      log.debug("EducationALNUpdateEvent: Invalid hmppsId: $hmppsId")
      throw ValidationException("Invalid HMPPS ID: $hmppsId")
    }

    val prisonNumber = personResponse.data?.nomisNumber ?: run { throw ValidationException("Invalid HMPPS ID: $hmppsId") }

    createAndPublishCuriousEducationALNUpdateEvent(prisonNumber = prisonNumber, detailUrl = request.detailUrl.toString(), description = request.status.name, request.requestId)

    return Response(HmppsMessageResponse(message = "Education ALN Assessment update event written to queue"))
  }

  fun createAndPublishCuriousEducationALNUpdateEvent(
    prisonNumber: String,
    detailUrl: String,
    description: String,
    externalReference: UUID,
  ) {
    log.info { "Publishing education ALN updated for prisoner [$prisonNumber]" }
    domainEventPublisher.createAndPublishEvent(
      prisonNumber = prisonNumber,
      occurredAt = Instant.now(),
      eventType = "prison.education-aln-assessment.updated",
      description = description,
      detailUrl = detailUrl,
      additionalInformation = mapOf("curiousExternalReference" to externalReference),
    )
  }
}
