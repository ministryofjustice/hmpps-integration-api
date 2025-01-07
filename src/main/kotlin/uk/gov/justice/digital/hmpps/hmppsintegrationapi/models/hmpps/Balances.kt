package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class AccountBalance(
  val accountCode: String? = null,
  val amount: Int? = null)

data class Balances(
  val accountBalances: Array<AccountBalance> = emptyArray()
)
