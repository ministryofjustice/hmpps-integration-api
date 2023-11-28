package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.CaseDetail

class CaseDetail(
  val nomsId: String? = null,
) {
  fun toCaseDetail(): CaseDetail = CaseDetail(
    nomsId = this.nomsId,
  )
}
