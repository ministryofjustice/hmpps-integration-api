package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactEvent
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactEvents
import java.time.ZoneId
import java.time.ZonedDateTime

data class NDeliusContactEvents(
  val totalResults: Int,
  val totalPages: Int,
  val content: List<NDeliusContactEvent>,
) {
  fun toPaginated(
    perPage: Int,
    pageNo: Int,
  ): ContactEvents =
    ContactEvents(
      content = content.map { it.toContactEvent() },
      count = content.size,
      page = pageNo,
      totalCount = totalResults.toLong(),
      totalPages = totalPages,
      isLastPage = pageNo * perPage >= totalResults,
      perPage = perPage,
    )
}

data class DeliusRefdata(
  val code: String,
  val description: String,
)

data class DeliusName(
  val forename: String,
  val surname: String,
) {
  fun toName() = "$forename $surname"
}

data class DeliusOfficer(
  val code: String,
  val name: DeliusName,
  val team: DeliusTeam,
)

data class DeliusPdu(
  val code: String,
  val description: String,
  val provider: DeliusRefdata,
)

data class DeliusTeam(
  val code: String,
  val description: String,
  val pdu: DeliusPdu,
)

data class NDeliusContactEvent(
  val id: Long,
  val crn: String,
  val createdAt: ZonedDateTime,
  val updatedAt: ZonedDateTime,
  val contactDate: ZonedDateTime,
  val type: DeliusRefdata,
  val description: String,
  val location: DeliusRefdata?,
  val outcome: DeliusRefdata?,
  val officer: DeliusOfficer,
  val notes: String?,
) {
  fun toContactEvent(): ContactEvent =
    ContactEvent(
      id,
      crn,
      createdAt.withZoneSameInstant(ZoneId.of("UTC")),
      updatedAt.withZoneSameInstant(ZoneId.of("UTC")),
      contactDate.withZoneSameInstant(ZoneId.of("UTC")),
      type.description,
      outcome?.description,
      location?.description,
      officer.team.pdu.provider.description,
      officer.team.pdu.description,
      officer.team.code,
      officer.team.description,
      officer.code,
      officer.name.toName(),
      description,
      notes,
    )
}
