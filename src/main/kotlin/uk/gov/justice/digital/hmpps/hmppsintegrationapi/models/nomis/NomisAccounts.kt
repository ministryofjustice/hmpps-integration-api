package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

data class NomisAccounts(
  val spends: Int,
  val savings: Int,
  val cash: Int,
)