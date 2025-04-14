package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.caseNotes

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseNote
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.OCNPagination

data class PaginatedCaseNotes(
  val content: List<CaseNote>,
  val pagination: OCNPagination,
)
