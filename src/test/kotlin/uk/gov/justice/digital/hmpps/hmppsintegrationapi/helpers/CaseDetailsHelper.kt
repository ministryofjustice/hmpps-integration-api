package uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.CaseDetail

fun generateCaseDetail(
  nomsId: String? = "ABC123",
): CaseDetail = CaseDetail(
  nomsId = nomsId,
)
