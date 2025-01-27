package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class TransactionTransferRequest(
  val description: String?,
  val amount: Int,
  val clientTransactionId: String,
  val clientUniqueRef: String,
) {
  fun toApiConformingMap(): Map<String, Any?> =
    mapOf(
      "description" to description,
      "amount" to amount,
      "client_transaction_id" to clientTransactionId,
      "client_unique_ref" to clientUniqueRef,
    ).filterValues { it != null }
}
