package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class PayRate(
  val incentiveCode: String,
  val incentiveLevel: String,
  val prisonPayBand: PrisonPayBand,
  val rate: Int,
  val pieceRate: Int,
  val pieceRateItems: Int,
)
