package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PayRate
import java.time.LocalDate

data class ActivitiesPayRate(
  val id: Long,
  val incentiveNomisCode: String,
  val incentiveLevel: String,
  val prisonPayBand: ActivitiesPrisonPayBand,
  val rate: Int?,
  val pieceRate: Int?,
  val pieceRateItems: Int?,
  val startDate: LocalDate?,
) {
  fun toPayRate() =
    PayRate(
      incentiveCode = this.incentiveNomisCode,
      incentiveLevel = this.incentiveLevel,
      prisonPayBand = this.prisonPayBand.toPrisonPayBand(),
      rate = this.rate,
      pieceRate = this.pieceRate,
      pieceRateItems = this.pieceRateItems,
    )
}
