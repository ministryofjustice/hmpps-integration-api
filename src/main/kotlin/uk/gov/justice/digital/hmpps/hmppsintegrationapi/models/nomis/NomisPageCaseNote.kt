package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseNote
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseNoteAmendment

data class NomisPageCaseNote(
  val content: List<NomisCaseNote> = listOf(),
) {
  fun toCaseNotes(): List<CaseNote> =
    this.content.map {
      val amendments =
        it.amendments.stream().map {
            amendment ->
          CaseNoteAmendment(amendment?.caseNoteAmendmentId, amendment?.creationDateTime, amendment?.additionalNoteText)
        }.toList()
      CaseNote(
        it.caseNoteId,
        it.offenderIdentifier,
        it.type,
        it.typeDescription,
        it.subType,
        it.subTypeDescription,
        it.creationDateTime,
        it.occurrenceDateTime,
        it.text,
        it.locationId,
        it.sensitive,
        amendments,
      )
    }
}
