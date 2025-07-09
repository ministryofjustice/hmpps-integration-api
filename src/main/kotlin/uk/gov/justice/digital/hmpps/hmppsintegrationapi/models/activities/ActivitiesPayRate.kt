package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PayRate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonPayBand

data class ActivitiesPayRate(
  val id: Long,
  val incentiveNomisCode: String,
  val incentiveLevel: String,
  val prisonPayBand: PrisonPayBand,
  val rate: Int,
  val pieceRate: Int,
  val pieceRateItems: Int,
) {
  fun toPayRate() =
    PayRate(
      incentiveCode = this.incentiveNomisCode,
      incentiveLevel = this.incentiveLevel,
      prisonPayBand = this.prisonPayBand,
      rate = this.rate,
      pieceRate = this.pieceRate,
      pieceRateItems = this.pieceRateItems,
    )
}
