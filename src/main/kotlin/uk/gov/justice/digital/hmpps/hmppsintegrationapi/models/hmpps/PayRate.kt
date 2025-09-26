package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class PayRate(
  @Schema(description = "The code for the incentive/earned privilege level", example = "BAS")
  val incentiveCode: String,
  @Schema(description = "The prisoner's incentive/earned privilege level", example = "standard")
  val incentiveLevel: String,
  @Schema(description = "The pay band for this activity pay")
  val prisonPayBand: PrisonPayBand,
  @Schema(description = "The earning rate for one half day session for someone of this incentive level and pay band (in pence)", example = "150")
  val rate: Int?,
  @Schema(description = "Where payment is related to produced amounts of a product, this indicates the payment rate (in pence) per pieceRateItems produced", example = "150")
  val pieceRate: Int?,
  @Schema(description = "Where payment is related to the number of items produced in a batch of a product, this is the batch size that attract 1 x pieceRate", example = "10")
  val pieceRateItems: Int?,
  @Schema(description = "The effective start date for this pay rate", example = "2024-06-18")
  val startDate: LocalDate?,
)
