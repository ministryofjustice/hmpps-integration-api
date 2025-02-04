package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class TransactionTransferCreateResponse(
  @Schema(description = "Transaction id for the debit portion of the transaction.")
  var debitTransactionId: String,
  @Schema(description = "Transaction id for the credit portion of the transaction.")
  var creditTransactionId: String,
  @Schema(description = "Transaction id for the whole transaction.")
  var transactionId: String,
)
