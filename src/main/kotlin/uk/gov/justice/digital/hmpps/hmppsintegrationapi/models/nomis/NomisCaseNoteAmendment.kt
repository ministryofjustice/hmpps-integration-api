package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import java.time.LocalDateTime

data class NomisCaseNoteAmendment(
  val caseNoteAmendmentId: String? = null,
  val creationDateTime: LocalDateTime? = null,
  val additionalNoteText: String? = null,
)
