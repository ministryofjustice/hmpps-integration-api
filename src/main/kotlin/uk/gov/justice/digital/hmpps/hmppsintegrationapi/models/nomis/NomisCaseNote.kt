package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import java.time.LocalDateTime

data class NomisCaseNote(
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
  val amendments: List<NomisCaseNoteAmendment?> = emptyList(),
)
