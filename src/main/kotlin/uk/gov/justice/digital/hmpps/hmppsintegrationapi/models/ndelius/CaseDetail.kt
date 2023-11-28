package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

class CaseDetail(
  val nomsId: String? = null,
) {
  fun toCaseDetail(): CaseDetail = CaseDetail(
    nomsId = this.nomsId,
  )
}
