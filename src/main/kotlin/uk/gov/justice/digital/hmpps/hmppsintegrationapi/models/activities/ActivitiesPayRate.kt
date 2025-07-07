package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonPayBand

data class ActivitiesPayRate(
  val id: Long,
  val incentiveNomisCode: String,
  val incentiveLevel: String,
  val prisonPayBand: PrisonPayBand,
  val rate: Int,
  val pieceRate: Int,
  val pieceRateItems: Int,
)
