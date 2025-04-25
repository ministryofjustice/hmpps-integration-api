package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseNote
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseNoteAmendment
import java.time.LocalDateTime

data class PrisonApiCaseNote(
  val caseNoteId: String,
  val offenderIdentifier: String? = null,
  val type: String? = null,
  val typeDescription: String? = null,
  val subType: String? = null,
  val subTypeDescription: String? = null,
  val creationDateTime: LocalDateTime? = null,
  val occurrenceDateTime: LocalDateTime? = null,
  val text: String? = null,
  val locationId: String? = null,
  val sensitive: Boolean = false,
  val amendments: List<PrisonApiCaseNoteAmendment?> = emptyList(),
) {
  fun toCaseNote(): CaseNote {
    val amendments =
      this.amendments
        .stream()
        .map { amendment ->
          CaseNoteAmendment(amendment?.caseNoteAmendmentId, amendment?.creationDateTime, amendment?.additionalNoteText)
        }.toList()
    return CaseNote(
      this.caseNoteId,
      this.offenderIdentifier,
      this.type,
      this.typeDescription,
      this.subType,
      this.subTypeDescription,
      this.creationDateTime,
      this.occurrenceDateTime,
      this.text,
      this.locationId,
      this.sensitive,
      amendments,
    )
  }
}
