package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import jakarta.validation.ValidationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.EducationStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.EducationStatusChangeRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import java.net.URI
import java.net.URL
import java.util.UUID

class EducationStatusServiceTest {
  private val getPersonService: GetPersonService = mock()
  private val domainEventPublisher: DomainEventPublisher = mock()
  private val service = EducationStatusService(getPersonService, domainEventPublisher)

  private val hmppsId = "A4321BC"
  private val nomisNumber = "A1234BC"
  private val requestId = UUID.randomUUID()
  private val detailUrl: URL = URI.create("http://meganexus.com/education/update").toURL()

  private val request =
    EducationStatusChangeRequest(
      status = EducationStatus.EDUCATION_STARTED,
      detailUrl = detailUrl,
      requestId = requestId,
    )

  @Test
  fun `sendEducationUpdateEvent publishes event with valid nomis number`() {
    val personResponse = Response<NomisNumber?>(data = NomisNumber(nomisNumber), errors = emptyList())

    whenever(getPersonService.getNomisNumber(eq(hmppsId))).thenReturn(personResponse)

    val response = service.sendEducationUpdateEvent(hmppsId, request)

    verify(domainEventPublisher).createAndPublishEvent(
      prisonNumber = eq(nomisNumber),
      occurredAt = any(),
      eventType = eq("prison.education.updated"),
      description = eq("EDUCATION_STARTED"),
      detailUrl = eq(detailUrl.toString()),
      additionalInformation = eq(mapOf("curiousExternalReference" to requestId)),
    )

    assertThat(response).isEqualTo(Response(HmppsMessageResponse(message = "Education status update event written to queue")))
  }

  @Test
  fun `sendEducationUpdateEvent throws ValidationException when nomis number is null`() {
    whenever(getPersonService.getNomisNumber(eq(hmppsId))).thenReturn(Response(data = null, errors = emptyList()))

    val exception =
      assertThrows<ValidationException> {
        service.sendEducationUpdateEvent(hmppsId, request)
      }

    assertThat(exception.message).isEqualTo("Invalid HMPPS ID: $hmppsId")
  }
}
