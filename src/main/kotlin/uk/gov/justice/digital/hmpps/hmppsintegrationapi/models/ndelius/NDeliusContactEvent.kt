package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactEvent
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactEvents
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.ContactEventStubGenerator.EuropeLondon
import java.time.ZonedDateTime

data class NDeliusContactEvents(
  val size: Int,
  val page: Int,
  val totalResults: Int,
  val totalPages: Int,
  val contactEvents: List<NDeliusContactEvent>,
) {
  fun toPaginated(): ContactEvents =
    ContactEvents(
      content = contactEvents.map { it.toContactEvent() },
      count = contactEvents.size,
      page = page,
      totalCount = totalResults.toLong(),
      totalPages = totalPages,
      isLastPage = page * size >= totalResults,
      perPage = size,
    )
}

data class NDeliusContactEvent(
  val contactEventIdentifier: Long,
  val offenderHmppsId: String,
  @JsonSerialize(using = ZonedDateTimeSerializer::class)
  val creationDateTime: ZonedDateTime,
  @JsonSerialize(using = ZonedDateTimeSerializer::class)
  val updateDateTime: ZonedDateTime,
  @JsonSerialize(using = ZonedDateTimeSerializer::class)
  val contactDateTime: ZonedDateTime,
  val contactType: String,
  val outcome: String,
  val area: String,
  val pdu: String,
  val teamId: Long,
  val teamName: String,
  val officerId: Long,
  val officerName: String,
  val description: String,
  val notes: String,
) {
  fun toContactEvent() =
    ContactEvent(
      contactEventIdentifier,
      offenderHmppsId,
      creationDateTime.toInstant().atZone(EuropeLondon).withZoneSameInstant(EuropeLondon),
      updateDateTime.toInstant().atZone(EuropeLondon).withZoneSameInstant(EuropeLondon),
      contactDateTime.toInstant().atZone(EuropeLondon).withZoneSameInstant(EuropeLondon),
      contactType,
      outcome,
      area,
      pdu,
      teamId,
      teamName,
      officerId,
      officerName,
      description,
      notes,
    )
}
