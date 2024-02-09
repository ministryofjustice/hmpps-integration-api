package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseNote
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PageCaseNote

data class NomisPageCaseNote(
  val content: List<NomisCaseNote> = listOf(),
) {
  fun toPageCaseNote(): PageCaseNote = PageCaseNote(
    caseNotes = this.content.map {
      CaseNote(caseNoteId = it.caseNoteId)
    },
  )
}
