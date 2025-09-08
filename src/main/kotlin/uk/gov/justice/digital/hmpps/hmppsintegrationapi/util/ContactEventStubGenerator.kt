package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.NDeliusContactEvent
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.NDeliusContactEvents
import java.time.LocalDateTime
import kotlin.math.ceil

object ContactEventStubGenerator {
  val baseDateTimeString = "2025-09-08T11:34:03.569"
  val baseDateTime = LocalDateTime.parse(baseDateTimeString)

  fun generateNDeliusContactEvent(
    id: Long,
    crn: String,
  ) = NDeliusContactEvent(
    contactEventIdentifier = id,
    contactType = "Contact Type for $id",
    creationDateTime = baseDateTime.minusDays(id),
    updateDateTime = baseDateTime.minusDays(id - 1),
    offenderHmppsId = crn,
    contactDateTime = baseDateTime.minusDays(id - 2),
    outcome = "Outcome",
    area = "area",
    pdu = "PDU",
    teamId = 1234L,
    teamName = "Team Name",
    officerId = 123456L,
    officerName = "Officer Name",
    description = "description",
    notes = "notes",
  )

  fun generateNDeliusContactEvents(
    crn: String,
    pageSize: Int,
    pageNumber: Int,
    totalRecords: Int,
  ): NDeliusContactEvents {
    val totalPages = ceil((totalRecords.toFloat() / pageSize.toFloat())).toInt()
    val idTo = if ((pageNumber * pageSize) > totalRecords) totalRecords else (pageNumber * pageSize)
    val idFrom = ((pageNumber - 1) * pageSize) + 1

    return NDeliusContactEvents(
      contactEvents =
        (idFrom..idTo).map {
          generateNDeliusContactEvent(it.toLong(), crn)
        },
      size = pageSize,
      page = pageNumber,
      totalResults = totalRecords,
      totalPages = totalPages,
    )
  }
}
