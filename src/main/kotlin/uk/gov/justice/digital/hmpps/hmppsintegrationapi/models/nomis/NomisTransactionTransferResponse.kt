package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

data class NomisTransactionTransferResponse(
  val debitTransaction: DebitTransaction,
  val creditTransaction: CreditTransaction,
  val transactionId: Long,
)

data class DebitTransaction(
  val id: String,
)

data class CreditTransaction(
  val id: String,
)
