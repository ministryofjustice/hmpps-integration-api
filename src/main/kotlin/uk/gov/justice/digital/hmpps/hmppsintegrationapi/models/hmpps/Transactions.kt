package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import java.util.stream.Collectors

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
  fun toTransactionList(): List<Transaction> =
    this.transactions
      .stream()
      .map {
        Transaction(
          id = it.id,
          type = it.type,
          description = it.description,
          amount = it.amount,
          date = it.date,
        )
      }.collect(Collectors.toList())
}
