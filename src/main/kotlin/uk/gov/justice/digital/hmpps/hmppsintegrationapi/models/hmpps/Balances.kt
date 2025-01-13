package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class AccountBalance(
  val accountCode: String,
  val amount: Int,
)

data class Balances(
  val balances: List<AccountBalance> = emptyList(),
)

data class Balance(
  val balance: AccountBalance,
)
