package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class TransactionTransferRequest(
  @field:NotBlank(message = "Description must not be blank")
  @Schema(description = "Description of the transaction.")
  val description: String,
  @Schema(
    description = "Amount of money in pence, must be positive.",
    example = "1234",
  )
  val amount: Int,
  @field:NotBlank(message = "Client transaction ID must not be blank")
  @Schema(description = "Client Transaction Id.")
  val clientTransactionId: String,
  @field:NotBlank(message = "Client unique ref must not be blank")
  @Schema(description = "A reference unique to the client making the post. Maximum size 64 characters, only alphabetic, numeric, '-' and '_' are allowed.")
  val clientUniqueRef: String,
  @field:NotBlank(message = "From account must not be blank")
  @Schema(description = "The account to move money from. Must be 'spends'.", example = "spends")
  val fromAccount: String,
  @field:NotBlank(message = "To account must not be blank")
  @Schema(description = "The account to move money to. Must be 'savings'.", example = "savings")
  val toAccount: String,
) {
  fun toApiConformingMap(): Map<String, Any?> =
    mapOf(
      "description" to description,
      "amount" to amount,
      "client_transaction_id" to clientTransactionId,
      "client_unique_ref" to clientUniqueRef,
    )
}
