package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class TransactionTransferCreateResponse(
  var debitTransactionId: String,
  var creditTransactionId: String,
  var transactionId: String,
)
