package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class Type(
  val code: String,
  val desc: String,
)

data class Transaction(
  val id: String,
  val type: Type,
  val description: String,
  val amount: Int,
  val date: String,
)

data class Transactions(
  val transactions: List<Transaction> = emptyList(),
) {
  fun toTransactionList(): List<Transaction> = this.transactions
}
