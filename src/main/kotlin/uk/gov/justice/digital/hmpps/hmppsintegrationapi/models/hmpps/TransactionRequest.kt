package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class TransactionRequest(
  val type: String, // reject if not in enum
  val description: String?,
  val amount: Int,
  val clientTransactionId: String,
  val clientUniqueRef: String,
) {
  fun toMap(): Map<String, Any?> =
    mapOf(
      "type" to type,
      "description" to description,
      "amount" to amount,
      "clientTransactionId" to clientTransactionId,
      "clientUniqueRef" to clientUniqueRef,
    )
}
