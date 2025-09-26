package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactEvent
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactEvents
import java.time.LocalDateTime
import java.time.ZoneId

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
  val creationDateTime: LocalDateTime,
  val updateDateTime: LocalDateTime,
  val contactDateTime: LocalDateTime,
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
  fun toContactEvent(): ContactEvent {
    val europeLondon: ZoneId = ZoneId.of("Europe/London")
    return ContactEvent(
      contactEventIdentifier,
      offenderHmppsId,
      creationDateTime.atZone(europeLondon).withZoneSameInstant(ZoneId.of("UTC")),
      updateDateTime.atZone(europeLondon).withZoneSameInstant(ZoneId.of("UTC")),
      contactDateTime.atZone(europeLondon).withZoneSameInstant(ZoneId.of("UTC")),
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
}
