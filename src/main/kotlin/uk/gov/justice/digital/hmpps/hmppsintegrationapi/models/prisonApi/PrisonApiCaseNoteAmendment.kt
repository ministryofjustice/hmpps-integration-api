package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi

import java.time.LocalDateTime

data class PrisonApiCaseNoteAmendment(
  val caseNoteAmendmentId: String? = null,
  val creationDateTime: LocalDateTime? = null,
  val additionalNoteText: String? = null,
)
