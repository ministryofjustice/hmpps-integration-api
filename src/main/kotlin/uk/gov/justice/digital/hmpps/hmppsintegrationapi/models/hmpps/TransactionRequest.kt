package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class TransactionRequest(
  @field:NotBlank(message = "Transaction type must not be blank")
  @Schema(description = "Transaction type. Determines which accounts will be debited and credited.", example = "CANT")
  var type: String,
  @Schema(description = "Description of the transaction.")
  val description: String?,
  @Schema(
    description = "Amount of money in pence, must be positive. Whether the transaction is a credit or debit is determined by the type.",
    example = "1234",
  )
  val amount: Int,
  @Schema(description = "Client Transaction Id.")
  val clientTransactionId: String,
  @Schema(description = "A reference unique to the client making the post. Maximum size 64 characters, only alphabetic, numeric, '-' and '_' are allowed.")
  val clientUniqueRef: String,
) {
  fun toApiConformingMap(): Map<String, Any?> =
    mapOf(
      "type" to type,
      "description" to description,
      "amount" to amount,
      "client_transaction_id" to clientTransactionId,
      "client_unique_ref" to clientUniqueRef,
    ).filterValues { it != null }
}
