package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class AccountBalance(
  val accountCode: String,
  val amount: Int,
)

data class Balances(
  val accountBalances: List<AccountBalance> = emptyList(),
)
