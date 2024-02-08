package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseNote
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PageCaseNote

data class NomisPageCaseNote(
  val caseNotes: List<NomisCaseNote> ? = emptyList(),
) {
  fun toPageCaseNote(): PageCaseNote = PageCaseNote(
    caseNotes = this.caseNotes?.map {
      CaseNote(caseNoteId = it.caseNoteId)
    },
  )
}
