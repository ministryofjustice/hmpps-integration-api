package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PageCaseNote

data class NomisPageCaseNote(
  val caseNoteId: String,
) {
  fun toPageCaseNote(): PageCaseNote = PageCaseNote(
    caseNoteId = this.caseNoteId,
  )
}
