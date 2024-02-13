package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseNote

data class NomisPageCaseNote(
  val content: List<NomisCaseNote> = listOf(),
) {
  fun toCaseNotes(): List<CaseNote> = this.content.map {
    CaseNote(caseNoteId = it.caseNoteId)
  }
}
