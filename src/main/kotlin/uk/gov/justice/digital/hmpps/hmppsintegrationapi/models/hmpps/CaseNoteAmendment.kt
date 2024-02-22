package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import java.time.LocalDateTime

data class CaseNoteAmendment(
  val caseNoteAmendmentId: Long? = null,
  val creationDateTime: LocalDateTime? = null,
  val additionalNoteText: String? = null,
)
