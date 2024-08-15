package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class TopupSupervision(
  @Schema(description = "(calculated) - top-up supervision expiry date for offender.", example = "2023-03-01")
  val expiryCalculatedDate: LocalDate? = null,
  @Schema(description = "top-up supervision expiry date for offender.", example = "2023-03-01")
  val expiryDate: LocalDate? = null,
  @Schema(description = "(override) - top-up supervision expiry date for offender.", example = "2023-03-01")
  val expiryOverrideDate: LocalDate? = null,
  @Schema(description = "Top-up supervision start date for offender - calculated as licence end date + 1 day or releaseDate if licence end date not set.", example = "2023-03-01")
  val startDate: LocalDate? = null,
)
