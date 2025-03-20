package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonerAlerts

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedAlerts

data class PAPaginatedAlerts(
  val content: List<PAAlert>,
  val totalElements: Long,
  val totalPages: Int,
  val first: Boolean,
  val last: Boolean,
  val size: Int,
  val number: Int,
  val sort: PASort,
  val numberOfElements: Int,
  val pageable: PAPageable,
  val empty: Boolean,
) {
  fun toPaginatedAlerts(pndOnly: Boolean = false) =
    PaginatedAlerts(
      content = (if (pndOnly) this.content.filter { isPndAlert(it) } else this.content).map { it.toAlert() },
      totalPages = this.totalPages,
      totalCount = this.totalElements,
      isLastPage = this.last,
      count = this.numberOfElements,
      page = this.number + 1, // Alerts API pagination is 0 based
      perPage = this.size,
    )

  private fun isPndAlert(alert: PAAlert): Boolean =
    alert.alertCode.code in
      listOf(
        "BECTER",
        "HA",
        "XA",
        "XCA",
        "XEL",
        "XELH",
        "XER",
        "XHT",
        "XILLENT",
        "XIS",
        "XR",
        "XRF",
        "XSA",
        "HA2",
        "RCS",
        "RDV",
        "RKC",
        "RPB",
        "RPC",
        "RSS",
        "RST",
        "RDP",
        "REG",
        "RLG",
        "ROP",
        "RRV",
        "RTP",
        "RYP",
        "HS",
        "SC",
      )
}
